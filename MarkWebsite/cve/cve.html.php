<?php include '../include/header.inc.html.php'; ?>
<div class="row">
  <div class="title">
    <h1>CVE</h1>
  </div>
</div>
<div class="row">
  <form method="post">
    <a class="btn btn-primary" href="../vulnerability/?vid=<?php echo $_GET['vid']?>">
      vulnerability info
    </a>
    <button class="btn btn-primary btn-right" type="submit" name="next_cve">
      next cve
      <span class="glyphicon glyphicon-menu-right"></span>
    </button>
    <button class="btn btn-primary btn-right" type="submit" name="prev_cve">
      <span class="glyphicon glyphicon-menu-left"></span>
      prev cve
    </button>
  </form>
</div>
<div class="row">
  <h3>Basic Info:</h3>
</div>
<div class="row content-line">
  <div class="col-md-2 content-label">
    CVE Title:
  </div>
  <div class="col-md-offset-2">
    <?php echo $result['cve_title']; ?>
  </div>
</div>
<div class="row content-line">
  <div class="col-md-2 content-label">
    Severity:
  </div>
  <div class="col-md-offset-2">
    <?php echo $result['severity']; ?>
  </div>
</div>
<div class="row content-line">
  <div class="col-md-2 content-label">
    Date:
  </div>
  <div class="col-md-offset-2">
    <?php echo $result['date']; ?>
  </div>
</div>
<div class="row content-line">
  <div class="col-md-2 content-label">
    Bug:
  </div>
  <div class="col-md-offset-2">
    <?php echo $result['bug']; ?>
  </div>
</div>
<div class="row content-line">
  <div class="col-md-2 content-label">
    Device:
  </div>
  <div class="col-md-offset-2">
    <?php echo $result['device']; ?>
  </div>
</div>
<div class="row content-line">
  <div class="col-md-2 content-label">
    Version:
  </div>
  <div class="col-md-offset-2">
    <?php echo $result['version']; ?>
  </div>
</div>
<br />
<div class="row">
  <h3>Git Diff:</h3>
</div>
<div class="row">
  <pre>
    <code>
      <?php
        if (empty($git_diff)) {
          echo "Git Diff Not Available";
        }
        else {
          foreach ($git_diff as $line) {
            if (!empty($line)) {
              $line = str_replace("<", "&lt;", $line);
              $line = str_replace(">", "&gt;", $line);
              if ($line[0] == '-') {
                echo "<div class=\"code-block code-block-minus\">".$line."</div><br />";
              }
              elseif ($line[0] == '+') {
                echo "<div class=\"code-block code-block-plus\">".$line."</div><br />";
              }
              else {
                echo "<div class=\"code-block\">".$line."</div><br />";
              }
            }
          }
        }
      ?>
    </code>
  </pre>
</div>
<br />
<div class="row">
  <h3>Search In Repo:</h3>
</div>
<div class="row">
  <form method="post">
    <div class="form-group">
      <div class="input-group">
        <input type="text" class="form-control" placeholder="Input search subject here." name="search_input" required="true">
          <span class="input-group-btn">
            <button class="btn btn-primary" type="submit" name="search">
              <span class="glyphicon glyphicon-search"></span>
            </button>
          </span>
      </div>
    </div>
  </form>
</div>
<div class="row">
  <pre>
    <code>
      <?php
        if (empty($search_result)) {
          echo "No Files Found Containing Subject";
        }
        else {
          echo "<div class=\"code-block\">Found Subject In Files:</div><br />";
          foreach ($search_result as $line) {
            if (!empty($line)) {
              echo "<div class=\"code-block\"><a href=\"../repo/".$repo_name_refined."/".$line."\">"
                  .$line."</a></div><br />";
            }
          }
        }
      ?>
    </code>
  </pre>
</div>
<br />
<div class="row">
  <h3>Mark CVE:</h3>
</div>
<div class="row">
  <form method="post">
    <div class="form-group">
      <div class="checkbox">
        <label>
          <input type="checkbox" name="genprog_checkbox"
          <?php
            if (!empty($genprog_checked)) {
              echo " checked";
            }
          ?>
          >
          GenProg
        </label>
      </div>
      <div class="checkbox">
        <label>
          <input type="checkbox" name="spr_checkbox"
            <?php
              if (!empty($spr_checked)) {
                echo " checked";
              }
            ?>
          >
          SPR
        </label>
      </div>
      <button class="btn btn-primary" type="submit" name="mark">
        Save
      </button>
    </div>
  </form>
</div>
<br />
<div class="row">
  <h3>Comment Area:</h3>
</div>
<form method="post">
  <div class="form-group">
    <div class="row">
      <textarea class="form-control" rows="5" name="comment_content" required="true"></textarea>
    </div>
    <br />
    <div class="row">
      <button class="btn btn-primary btn-right" type="submit" name="comment">
        Comment
      </button>
    </div>
  </div>
</form>
<?php if (!empty($comment_result)): ?>
  <div class="row">
    <table class="table table-bordered">
      <?php foreach ($comment_result as $comment_record): ?>
        <tr>
          <td class="comment-item">
            <?php echo $comment_record['date']; ?><br />
            <form method="post">
              <input type="hidden" name="comment_id" value="<?php echo $comment_record['id']; ?>">
              <button class="btn btn-delete" type="submit" name="delete">
                Delete
              </button>
            </form>
          </td>
          <td>
            <?php echo $comment_record['content']; ?>
          </td>
        </tr>
      <?php endforeach; ?>
    </table>
  </div>
<?php endif; ?>
<?php include '../include/footer.inc.html.php'; ?>
