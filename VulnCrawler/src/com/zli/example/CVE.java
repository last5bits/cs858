package com.zli.example;

import java.util.HashMap;

public class CVE
{
    private static final String DB_VULNERABILITY_ID = "vulnerability_id";
    private static final String DB_CVE_TITLE = "cve_title";
    private static final String DB_SEVERITY = "severity";
    private static final String DB_DATE = "date";
    private static final String DB_BUG = "bug";
    private static final String DB_DEVICE = "device";
    private static final String DB_VERSION = "version";
    private static final String DB_REPO_NAME = "repo_name";
    private static final String DB_COMMIT = "commit";
    private static final String DB_PARENT_COMMIT = "parent_commit";
    
    private HashMap<String, String> fieldMap;
    
    public CVE() {
        fieldMap = new HashMap<>();
        fieldMap.put(DB_VULNERABILITY_ID, "");
        fieldMap.put(DB_CVE_TITLE, "");
        fieldMap.put(DB_SEVERITY, "");
        fieldMap.put(DB_DATE, "");
        fieldMap.put(DB_BUG, "");
        fieldMap.put(DB_DEVICE, "");
        fieldMap.put(DB_VERSION, "");
        fieldMap.put(DB_REPO_NAME, "");
        fieldMap.put(DB_COMMIT, "");
        fieldMap.put(DB_PARENT_COMMIT, "");
    }
    
    public HashMap<String, String> getFieldMap() {
        return fieldMap;
    }

    public void insertDatabase(int vulnerabilityId) {
        String sql = "INSERT INTO cve (v_id, cve_title, severity, date, bug, device, version, "
                + "repo_name, commit, parent_commit) VALUES (" + vulnerabilityId + ", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_CVE_TITLE)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_SEVERITY)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_DATE)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_BUG)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_DEVICE)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_VERSION)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_REPO_NAME)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_COMMIT)) + "\", " +
                "\"" + HtmlParser.clean(fieldMap.get(DB_PARENT_COMMIT)) + "\");";
        Database database = new Database();
        database.executeSQL(sql);
    }
    
    /**
     * Get commit id and parent commit id of a CVE record.
     */
    public void update() {
        String bug = fieldMap.get(DB_BUG);
        if (bug.contains("href")) {
            try {
                int linkStartIndex = bug.indexOf("href=\"");
                linkStartIndex += "href=\"".length();
                int linkEndIndex = bug.indexOf("\"", linkStartIndex);
                String url = bug.substring(linkStartIndex, linkEndIndex);
                int commitStartIndex = Math.max(url.lastIndexOf('/'), url.lastIndexOf('='));
                String commit = url.substring(commitStartIndex + 1, url.length());
                fieldMap.put(DB_COMMIT, commit);

                // Who needs encryption?
                // On a serious note, git.kernel.org for some reason does not work
                // over https (java.net.SocketException: Connection reset)
                if (url.startsWith("https://git.kernel.org")) {
                    url = url.replace("https://", "http://");
                }
                
                Http http = new Http();
                String html = http.httpGet(url);

                final boolean pageOk = html != null && !html.equals("");
                if (pageOk) {
                    // Get parent commit id.
                    String parentCommit = getParentCommit(url, html);
                    fieldMap.put(DB_PARENT_COMMIT, parentCommit);

                    // Get repo name
                    String repoName = getRepoName(url, html);
                    fieldMap.put(DB_REPO_NAME, repoName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getParentCommit(String url, String html) {
        if (!html.contains("parent")) {
            return "";
        }

        int parentStartIndex = html.indexOf("parent");
        parentStartIndex += "parent".length();
        while (html.charAt(parentStartIndex) == '<') {
            int endOfTag = html.indexOf('>', parentStartIndex);
            parentStartIndex = endOfTag + 1;
        }
        int parentEndIndex = html.indexOf('<', parentStartIndex);
        String parentCommit = html.substring(parentStartIndex, parentEndIndex);

        return parentCommit;
    }

    private String getRepoName(String url, String html) {
        // wordNo-th word in the title is the repo name
        int wordNo = 0;
        if (url.contains(RepoWebSite.GoogleSource)) {
            wordNo = 2;
        }

        String titleOpenTag = "<title>";
        String titleCloseTag = "</title>";
        final int titleStartIndex = html.indexOf(titleOpenTag);
        final int titleEndIndex = html.indexOf(titleCloseTag);
        String title = html.substring(titleStartIndex + titleOpenTag.length(), titleEndIndex);

        String[] titleWords = title.split("\\s+");
        assert titleWords.length >= wordNo;
        String repoName = titleWords[wordNo];

        // put "android/" in front of the repo name
        if (url.contains(RepoWebSite.GoogleSource)) {
            repoName = "android/" + repoName;
        }

        return repoName;
    }
    
    public void print() {
        System.out.println(DB_CVE_TITLE + " : " + fieldMap.get(DB_CVE_TITLE));
        System.out.println(DB_SEVERITY + " : " + fieldMap.get(DB_SEVERITY));
        System.out.println(DB_DATE + " : " + fieldMap.get(DB_DATE));
        System.out.println(DB_BUG + " : " + fieldMap.get(DB_BUG));
        System.out.println(DB_DEVICE + " : " + fieldMap.get(DB_DEVICE));
        System.out.println(DB_VERSION + " : " + fieldMap.get(DB_VERSION));
        System.out.println(DB_REPO_NAME + " : " + fieldMap.get(DB_REPO_NAME));
        System.out.println();
    }
    
    public static String getFieldName(String originHead) {
        originHead = originHead.toLowerCase();
        if (originHead.contains("cve")) return DB_CVE_TITLE;
        if (originHead.contains("severity")) return DB_SEVERITY;
        if (originHead.contains("date reported")) return DB_DATE;
        if (originHead.contains("bug") || originHead.contains("reference")) return DB_BUG;
        if (originHead.contains("device")) return DB_DEVICE;
        if (originHead.contains("version")) return DB_VERSION;
        return "";
    }

    public static void main(String args[]) {
    }
}
