# Vulnerability Crawler
This is a crawler program written in Java that parses the information about security vulnerabilities in [Android](http://source.android.com/security/bulletin/) and corresponding commit information in [Google Source](https://android.googlesource.com/) or [CodeAurora](https://source.codeaurora.org/), and puts it into a MySQL database for ease of use. Courtesy of Zijin Li (z542li at uwaterloo dot ca).

## Setting Up the Program
1. Import the project to your Java IDE, e.g. [Eclipse](https://eclipse.org/).
2. Add `mysql-connector-java-5.1.17-bin.jar` to build path. In ecplipse you can do this by right clicking on the project name in `Package Explorer`, then `Build Path -> Configure Build Path...`, choose the `Libraries` tab, click `Add JARs...` on the right and choose this jar file.
3. Change the database URL, name, username and password in `Database.java` to the corresponding information of your database.
4. Add the root certificate of [CodeAurora](https://source.codeaurora.org/) to Java truststore by following the steps below:

* Get the root certificate file using a web browser. For example, in [Chrome](https://www.google.ca/chrome/browser/desktop/index.html) you can do this by clicking the lock icon on the left of the URL, then `Details -> View certificate -> Certification Path -> Choose the root certificate on the top -> View Certificate -> Details -> Copy to File...`.

* Add the certificate to truststore with keytool command:

```
$ keytool -import
	-alias your_alias
	-file path_to_downloaded_certificate
	-keystore path_to_java_keystore
```

* (The default password for the JVM truststore is `changeit`)

## Setting Up MySQL
Create `vulnerability` table and `cve` table with following commands:

```
CREATE TABLE `vulnerability` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=371 DEFAULT CHARSET=utf8;
```

```
CREATE TABLE `cve` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `v_id` int(10) unsigned NOT NULL,
  `cve_title` varchar(255) DEFAULT NULL,
  `severity` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `bug` varchar(255) DEFAULT NULL,
  `device` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `commit` varchar(255) DEFAULT NULL,
  `parent_commit` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `v_id` (`v_id`),
  CONSTRAINT `cve_ibfk_1` FOREIGN KEY (`v_id`) REFERENCES `vulnerability` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=637 DEFAULT CHARSET=utf8;
```

## Running the Program
Run the `main` method in `HtmlParser.java`.
