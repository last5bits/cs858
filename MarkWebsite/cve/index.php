<?php

error_reporting(E_ALL);
ini_set('display_errors', '1');

require_once '../include/db.inc.php';

$stmt = $conn->prepare("SELECT * FROM cve WHERE id = ?");
$stmt->bind_param("i", $id);
$id = $_GET['cid'];
$stmt->execute();
$result = $stmt->get_result()->fetch_assoc();
$genprog_checked = $result['genprog'];
$spr_checked = $result['spr'];
$repo_name_refined = str_replace('/', '@', $result['repo_name']);

$stmt = $conn->prepare("SELECT * FROM comment WHERE c_id = ?");
$stmt->bind_param("i", $id);
$id = $_GET['cid'];
$stmt->execute();
$comment_result = $stmt->get_result();

$git_diff = "";
if (!empty($result['repo_name']) and
      !empty($result['commit']) and
      !empty($result['parent_commit'])) {

  $shell_command = "cd ../repo/".$repo_name_refined." ; git diff ".
                    $result['parent_commit']." ".$result['commit'];
  exec($shell_command, $git_diff);
}

//grep:
//-I: skip binary files
//-F: no regular experessions in the input (e.g., "*" is literally a star)
//-R: recursively
$search_result = "";
if (isset($_POST['search'])) {
  $search_subject = str_replace('"', '\"', $_POST['search_input']);
  $shell_command = "cd ../repo/".$repo_name_refined." ; git checkout ".
                    $result['parent_commit']." ; grep -I -F -R \"".
                    $search_subject."\"";
  $raw_result = "";
  exec($shell_command, $raw_result);
  $search_result = array();
  foreach ($raw_result as $record) {
    $file_name = explode(":", $record)[0];
    if (!in_array($file_name, $search_result)) {
      $search_result[] = $file_name;
    }
  }
}

if (isset($_POST['mark'])) {
  if (isset($_POST['genprog_checkbox'])) {
    $genprog_checked = 1;
  }
  else {
    $genprog_checked = 0;
  }
  if (isset($_POST['spr_checkbox'])) {
    $spr_checked = 1;
  }
  else {
    $spr_checked = 0;
  }

  $stmt = $conn->prepare("
    UPDATE cve
    SET genprog = ?, spr = ?, marked = 1
    WHERE id = ?");
  $stmt->bind_param("iii", $genprog, $spr, $id);
  $id = $_GET['cid'];
  $genprog = $genprog_checked;
  $spr = $spr_checked;
  $stmt->execute();
}

if (isset($_POST['comment'])) {
  $stmt = $conn->prepare("
    INSERT INTO comment (content, c_id, date)
    VALUES (?, ?, CURDATE())");
  $stmt->bind_param("si", $content, $c_id);
  $content = $_POST['comment_content'];
  $c_id = $_GET['cid'];
  $stmt->execute();

  header('Location:./?vid='.$_GET['vid'].'&cid='.$_GET['cid']);
}

if (isset($_POST['delete'])) {
  $stmt = $conn->prepare("DELETE FROM comment WHERE id = ?");
  $stmt->bind_param("i", $id);
  $id = $_POST['comment_id'];
  $stmt->execute();

  header('Location:./?vid='.$_GET['vid'].'&cid='.$_GET['cid']);
}

if (isset($_POST['prev_cve'])) {
  $stmt = $conn->prepare("
    SELECT id, v_id FROM cve WHERE id < ? ORDER BY id DESC LIMIT 1");
  $stmt->bind_param("i", $id);
  $id = $_GET['cid'];
  $stmt->execute();
  $id_record = $stmt->get_result()->fetch_assoc();
  if (empty($id_record)) {
    echo "<script type=\"text/javascript\">alert(\"This is already the first one.\")</script>";
  }
  else {
    header('Location:./?vid='.$id_record['v_id']."&cid=".$id_record['id']);
  }
}

if (isset($_POST['next_cve'])) {
  $stmt = $conn->prepare("
    SELECT id, v_id FROM cve WHERE id > ? ORDER BY id LIMIT 1");
  $stmt->bind_param("i", $id);
  $id = $_GET['cid'];
  $stmt->execute();
  $id_record = $stmt->get_result()->fetch_assoc();
  if (empty($id_record)) {
    echo "<script type=\"text/javascript\">alert(\"This is already the last one.\")</script>";
  }
  else {
    header('Location:./?vid='.$id_record['v_id']."&cid=".$id_record['id']);
  }
}

include './cve.html.php';
