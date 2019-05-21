package com.example.splunk;

import com.splunk.*;
import java.io.*;
import java.util.*;
import com.google.common.base.Splitter;

public class SplunkEngine   {

    private Service service;
    private Command command;
    private String query;
    private String earliestTime;
    private String latestTime;


    public SplunkEngine(String []args, String usage, String earliest, String latest)  {
        command = Command.splunk(usage);
        command.parse(args);

        if (command.args.length != 1)
            Command.error("Search expression required");

        query = command.args[0];
        this.earliestTime = earliest;
        this.latestTime = latest;


    }

    public void connect()    {
        HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
        service = Service.connect(command.opts);
    }

    private void validateQuery()   {

        // Check the syntax of the query.
        try {
            Args parseArgs = new Args("parse_only", true);
            service.parse(query, parseArgs);
        } catch (HttpException e) {
            String detail = e.getDetail();
            Command.error("query '%s' is invalid: %s", query, detail);
        }

    }


    public void simpleSearch()    throws IOException {

        Args oneshotSearchArgs = new Args();
        oneshotSearchArgs.put("output_mode", "json");

        InputStream stream = service.oneshotSearch(query, oneshotSearchArgs);
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        try {
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            try {
                int size = 1024;
                char[] buffer = new char[size];
                while (true) {
                    System.out.println("looping");
                    int count = reader.read(buffer);
                    if (count == -1) break;
                    writer.write(buffer, 0, count);
                }

                writer.write("\n");
            } finally {
                writer.close();
            }
        } finally {
            reader.close();
        }

    }

    public void searchEvents()    throws IOException {

        JobArgs inputArgs = new JobArgs();
        JobResultsArgs resultsArgs = new JobResultsArgs();
        resultsArgs.setOutputMode(JobResultsArgs.OutputMode.CSV);

        inputArgs.setEarliestTime(earliestTime);
        inputArgs.setLatestTime(latestTime);

        Job job = service.getJobs().create(query, inputArgs);

        while (!job.isDone()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        InputStream inpStream = job.getResults(resultsArgs);

        ResultsReaderCsv resultsReader = new ResultsReaderCsv(inpStream);
        Event event = null;
        while ((event = resultsReader.getNextEvent()) != null) {
            String finalCsvLine = "";

            for (String key: event.keySet()) {
                String value = event.get(key);
                value = value.replace("\n", "");
                finalCsvLine += value + ",";
            }
            if (finalCsvLine.endsWith(",")) {
                finalCsvLine = finalCsvLine.substring(0, finalCsvLine.length() - 1);

            }
            System.out.println(finalCsvLine);

        }


    }

}
