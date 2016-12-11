#!/usr/bin/env python3
# Go through all the patches
# For each patch, measure number of lines added and deleted

import pymysql
import os

host=""
port=3306
user=""
passwd=""
db="cs858"
repos_folder="repos"
output_file_path="stats"

class cd:
    """Context manager for changing the current working directory"""
    def __init__(self, newPath):
        self.newPath = os.path.expanduser(newPath)

    def __enter__(self):
        self.savedPath = os.getcwd()
        os.chdir(self.newPath)

    def __exit__(self, etype, value, traceback):
        os.chdir(self.savedPath)

def iterate_patches():
    """
    Returns a list of tuples.
    Each tuple is of the form (git_repo, parent_commit, commit).
    Omits records with empty repo/commit/parent commit.
    Omits ignored records.
    """
    connection = pymysql.connect(host=host, port=port, user=user, passwd=passwd, db=db)
    cursor = connection.cursor()
    # can be fixed by neither
    # cursor.execute('select repo_name, parent_commit, commit from cve where ignored <> 1 and genprog = 0 and spr = 0');
    # can be fixed by GenProg
    # cursor.execute('select repo_name, parent_commit, commit from cve where ignored <> 1 and genprog = 1');
    # can be fixed by SPR
    cursor.execute('select repo_name, parent_commit, commit from cve where ignored <> 1 and spr = 1');

    res = list(cursor);

    cursor.close()
    connection.close()

    return res

def convert_repo_name(repo_name):
    """
    Convert a repo_name of the form "a/b/c" to
    "actual_repo_folder/a@b@c"
    """
    refined_repo_name = repo_name.replace("/", "@")
    return os.path.join(repos_folder, refined_repo_name)

def parse_diff_output(output):
    """
    Accepts git diff --numstat output and
    returns (number of files changed, number of lines added, deleted)
    """
    lines = [line for line in output.split("\n") if line]
    num_added = 0
    num_deleted = 0
    num_files = 0
    for line in lines:
        num_files += 1
        tokens = line.split("\t")
        num_added += int(tokens[0])
        num_deleted += int(tokens[1])

    return (num_files, num_added, num_deleted)

def get_stats(repo, parent_commit, commit):
    """
    Goes into the "repo" git repo,
    gets git diff stats between parent_commit and commit.
    Note: git diff ignores whitespace
    Note: git diff ignores comments (TODO)
    Returns the number of changed files, number of lines added and deleted.
    """
    command = "git diff -w --numstat %s %s" % (parent_commit, commit)
    with cd(convert_repo_name(repo)):
        output = os.popen(command).read()
        (num_files, num_added, num_deleted) = parse_diff_output(output)
    return (num_files, num_added, num_deleted)

if __name__ == "__main__":
    with open(output_file_path, "w") as output_file:
        count = 1
        for patch in iterate_patches():
            (num_files, num_added, num_deleted) = get_stats(patch[0], patch[1], patch[2])
            print("%s;%s;%s" % (num_files, num_added, num_deleted), file=output_file)
            print(count)
            count += 1
