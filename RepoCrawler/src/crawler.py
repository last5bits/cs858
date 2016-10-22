#!/usr/bin/env python3

from urllib.request import urlopen
import json
import os
import shlex
import subprocess
import sys

from utils import cd


repos_file_name = "repos.json"
start_url = "https://android.googlesource.com/?format=JSON"
dest_dir_path = "./repos"


def main():
    repos_json = get_repos_json()
    repos = json.loads(repos_json)

    if not create_dest_dir():
        print("Target directory %s already exists, exiting" % dest_dir_path
                , file=sys.stderr)
        sys.exit(1)

    with cd(dest_dir_path):
        repo_no = 1
        for repo_name in sorted(repos.keys()):
            print("%s %s" % (repo_no, repo_name))
            
            # remove slashes from the name
            repo_dir_name = transform_name(repo_name)

            # clone
            clone_url = repos[repo_name]["clone_url"]
            git_clone(clone_url, repo_dir_name)

            print()
            repo_no += 1


def get_repos_json():
    res = ""

    # try to read the repo file from the hard drive
    try:
        with open(repos_file_name, "r") as repos_file:
            res = repos_file.read()

    except IOError: # if no luck, download it
        res = urlopen(start_url).read().decode('utf-8')

        # first line is funny, remove it
        res = res[res.find("\n") + 1:]

        # write the json on disk for later use
        with open(repos_file_name, "w+") as repos_file:
            repos_file.write(res)

    return res


def transform_name(name):
    """
    Transform the repo name from a/b/c to a@b@c
    """
    return name.replace("/", "@")


def create_dest_dir():
    """
    Create the destination directory if it does not exist.
    Return False if it already exists
    """
    if not os.path.isdir(dest_dir_path):
        os.makedirs(dest_dir_path)
        return True
    return False


def git_clone(clone_url, dir_name):
    cmd = "git clone %s %s" % (clone_url, dir_name)
    proc = subprocess.Popen(shlex.split(cmd))
    proc.communicate()

if __name__ == "__main__":
    main()
