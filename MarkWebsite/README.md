# Vulnerability-marking website

Allows to conveniently examine patches mined by VulnCrawler and decide whether a particular patch is in the search spaces of the GenProg and SPR automatic repair tools. Courtesy of Zijin Li (z542li at uwaterloo dot ca).

## Deploying the website

1. Modify the database previously created by VulnCrawler:

```
CREATE TABLE `comment` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `c_id` int(10) unsigned NOT NULL,
  `date` date NOT NULL,
  `content` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  FOREIGN KEY (`c_id`) REFERENCES `cve` (`id`)
);
ALTER TABLE `cve` ADD `genprog` tinyint(1) DEFAULT '0';
ALTER TABLE `cve` ADD `spr` tinyint(1) DEFAULT '0';
ALTER TABLE `cve` ADD `marked` tinyint(1) DEFAULT '0';
```

2. Set up the LAMP stack on your server:
Install Apache (we assume that the MySQL server is already installed):
```
$ sudo apt install apache2
```
Install PHP and the required library:
```
$sudo apt install php7.0 php7.0-mysql libapache2-mod-php7
```

3. Copy the website code:
Copy the entire directory "MarkWebsite" to the public folder of Apache. Normally it is "/var/www/html".
Put all the repos you downloaded to the "repo" folder.
In the "db.inc.php" file in "include" folder, change the database name, user name and password to corresponding ones.

Visit "http://localhost/MarkWebsite/" in your browser and you should be presented with a list of vulnerabilities to examine.
