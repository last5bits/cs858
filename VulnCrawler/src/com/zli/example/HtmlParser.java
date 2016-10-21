package com.zli.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class HtmlParser
{
    private static final String URL_START = "http://source.android.com/security/bulletin/";
    private static final String URL_END = ".html";
    private static final String DATE_START = "2016-10-01";
    private static final String DATE_END = "2015-07-01";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat ("yyyy-MM-dd");
    
    private Date date;
    private Date endDate;
    private String html;
    
    public List<Vulnerability> parseAll() throws ParseException {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        date = simpleDateFormat.parse(DATE_START);
        endDate = simpleDateFormat.parse(DATE_END);
        while (!date.equals(endDate)) {
            Http http = new Http();
            String urlStr = URL_START + simpleDateFormat.format(date) + URL_END;
            if (date.getMonth() == 3) {
                // April's url is different than other months
                urlStr = "http://source.android.com/security/bulletin/2016-04-02.html";
            }
            html = http.httpGet(urlStr);
            List<Vulnerability> subList = parseSingle();
            vulnerabilities.addAll(subList);
            
            // month - 1
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MONTH, -1);
            date = cal.getTime();
        }
        return vulnerabilities;
    }
    
    private List<Vulnerability> parseSingle() {
        List<Vulnerability> subList = new ArrayList<>();
        int index = 0;
        while (html.indexOf("</h3>", index) != -1) {
            // get title
            int titleEndIndex = html.indexOf("</h3>", index);
            int titleStartIndex = html.lastIndexOf(">", titleEndIndex);
            index = titleEndIndex + "</h3>".length();
            String title = html.substring(titleStartIndex + 1, titleEndIndex);
            if (title.equals("") || title.contains("summary") ||
                    title.contains("Questions and Answers")) continue;
            
            // get description
            int descriptionEndIndex = html.indexOf("<table>", index);
            String description = html.substring(index, descriptionEndIndex);
            index = descriptionEndIndex + "<table>".length();
            
            // create a new vulerability instane
            Vulnerability vulnerability = new Vulnerability(title, description,
                    simpleDateFormat.format(date));
            
            // get table of CVE records
            int tableEndIndex = html.indexOf("</table>", index);
            String table = html.substring(index, tableEndIndex);
            index = tableEndIndex + "</table>".length();
            
            // parse cve records
            List<CVE> cves = parseCVE(table);
            
            // add cve records to vulnerability instance
            vulnerability.setCves(cves);
            subList.add(vulnerability);
        }
        return subList;
    }
    
    private List<CVE> parseCVE(String table) {
        List<CVE> cves = new ArrayList<>();
        
        // get a list of heads, cast the string to correct field names in database
        List<String> headList = new ArrayList<>();
        int headRowStartIndex = table.indexOf("<tr>");
        int headRowEndIndex = table.indexOf("</tr>");
        String headRow = table.substring(headRowStartIndex + "<tr>".length(), headRowEndIndex);
        int headRowIndex = 0;
        while (headRow.indexOf("<th>", headRowIndex) != -1) {
            // get head
            int headStartIndex = headRow.indexOf("<th>", headRowIndex);
            int headEndIndex = headRow.indexOf("</th>", headRowIndex);
            headRowIndex = headEndIndex + "</th>".length();
            String head = headRow.substring(headStartIndex + "<th>".length(), headEndIndex);
            headList.add(CVE.getFieldName(head));
        }
        
        int tableIndex = headRowEndIndex + "</tr>".length();
        while (table.indexOf("<tr>", tableIndex) != -1) {
            int rowStartIndex = table.indexOf("<tr>", tableIndex);
            int rowEndIndex = table.indexOf("</tr>", tableIndex);
            tableIndex = rowEndIndex + "</tr>".length();
            String row = table.substring(rowStartIndex + "<tr>".length(), rowEndIndex);
            int fieldCt = 0;
            int rowIndex = 0;
            CVE cve = new CVE();
            while (row.indexOf("<td", rowIndex) != -1) {
                int fieldStartIndex = row.indexOf(">", rowIndex) + 1;
                int fieldEndIndex = row.indexOf("</td>", rowIndex);
                rowIndex = fieldEndIndex + "</td>".length();
                String field = row.substring(fieldStartIndex, fieldEndIndex);
                cve.getFieldMap().put(headList.get(fieldCt), field);
                fieldCt++;
            }
            cves.add(cve);
        }
        
        return cves;
    }
    
    public static String clean(String input) {
        input = input.replaceAll("<(.*?)>", "");
        input = input.replaceAll("\"", "");
        input = input.replaceAll("\'", "");
        input = input.replaceAll("\\s+", " ");
        return input;
    }
    
    public static void main(String[] args) {
        try {
            HtmlParser htmlParser = new HtmlParser();
            List<Vulnerability> vulnerabilities = htmlParser.parseAll();
            for (Vulnerability vulnerability: vulnerabilities) {
                vulnerability.updateCVE();
                vulnerability.insertDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
