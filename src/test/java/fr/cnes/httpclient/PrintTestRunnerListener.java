/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This file is part of DOI-server.
 *
 * This JSPNego is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * JSPNego is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.httpclient;


import java.util.Collections;
import org.junit.Ignore;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class PrintTestRunnerListener extends RunListener {
    
    /**
     * Color reset.
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * Black color.
     */
    public static final String ANSI_BLACK = "\u001B[30m";

    /**
     * Red color.
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * Green color.
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * Yellow color.
     */
    public static final String ANSI_YELLOW = "\u001B[33;1m";

    /**
     * Blue color.
     */
    public static final String ANSI_BLUE = "\u001B[34;1m";

    /**
     * Purple color.
     */
    public static final String ANSI_PURPLE = "\u001B[35m";

    /**
     * Cyan color.
     */
    public static final String ANSI_CYAN = "\u001B[36m";

    /**
     * White color.
     */
    public static final String ANSI_WHITE = "\u001B[37;1m";      

    private static final Description FAILED = Description.createTestDescription("failed", "failed");
    private static final Description SKIP = Description.createTestDescription("skip", "skip");
    private int nbTotalTestCases;
    //private int nbSkips = 0;
    //private int nbErrors = 0;
    
    public PrintTestRunnerListener() {
        super();
    }
    
    /**
     *
     * @param title
     */
    public static void testTitle(final String title) {
        System.out.println(ANSI_BLUE + "Testing: " + title + ANSI_RESET);
    }

    /**
     *
     * @param title
     * @param status
     */
    public static void testTitle(final String title, final ColorStatus status) {
        testTitle(title, status, null);
    }

    public static void testTitle(final String title, final ColorStatus status, final String skipMessage) {
        final String colorStatus;
        switch (status) {
            case SKIP:
                colorStatus = ANSI_YELLOW + ColorStatus.SKIP.name().toLowerCase() + ANSI_RESET;
                break;
            case FAILED:
                colorStatus = ANSI_RED + ColorStatus.FAILED.name().toLowerCase() + ANSI_RESET;
                break;
            default:
                colorStatus = ANSI_GREEN + ColorStatus.OK.name().toLowerCase() + ANSI_RESET;
        }

        String pt = String.join("", Collections.nCopies(80, "."));
        if (title.length() > pt.length()) {
            pt = "";
        } else {
            pt = pt.substring(title.length() - 1, pt.length() - 1);
        }

        String skipMessageDisplay = (skipMessage == null) ? "" : ANSI_PURPLE + "  (" + skipMessage + ")" + ANSI_RESET;
        System.out.println(
                ANSI_BLUE + "Testing: " + title + ANSI_RESET + pt + colorStatus + skipMessageDisplay);
    }
        
    
    public static void mavenInfoRun(int numberTestCases) {
        String message = "[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + ANSI_CYAN + numberTestCases + " test cases to run" + ANSI_RESET;
        System.out.println(message);        
    }

    public static void mavenTitle(Failure failure) {
        String message = "[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + ANSI_WHITE + "Running " + failure.
                getDescription().getClassName() + ANSI_RESET;
        System.out.println(message);
    }

    public static void mavenSkipMessage(Failure failure) {
        String message = "[" + ANSI_YELLOW + "WARNING" + ANSI_RESET + "] " + ANSI_YELLOW + "Tests" + ANSI_RESET + " run: " + failure.
                getDescription().testCount() + ", Failures: 0, Errors: 0, " + ANSI_YELLOW + "Skipped: " + failure.
                        getDescription().testCount() + ANSI_RESET + ", Time elapsed: 0 s - " + failure.
                        getMessage();
        System.out.println(message);
    }    
    
    public static void mavenResult(Result result, int total, int skip, int error) {
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "]");
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] Results (per test case) :");
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "]");
        if(result.wasSuccessful()) {
            if(skip > 0) {
                System.out.println("[" + ANSI_BLUE + "WARNING" + ANSI_RESET+ "] "+ANSI_GREEN+"Tests run: "+total+", Failures: "+error+", Skipped: "+skip+ANSI_RESET);
            } else {
                System.out.println("[" + ANSI_GREEN + "INFO" + ANSI_RESET +"] "+ANSI_GREEN+"Tests run: "+total+", Failures: "+error+", Skipped: "+skip+ANSI_RESET);                
            }
        } else {
            System.out.println("[" + ANSI_RED + "ERROR" + ANSI_RESET +"] "+ANSI_RED+"Tests run: "+total+", Failures: "+error+", Skipped: "+skip+ANSI_RESET);                
        }
    }   
    
    public static enum ColorStatus {
        SKIP,
        FAILED,
        OK
    }     

    @Override
    public void testRunStarted(Description description) throws Exception {
        this.nbTotalTestCases = description.testCount();
        mavenInfoRun(this.nbTotalTestCases);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        testTitle(failure.getDescription().getMethodName(),ColorStatus.FAILED);
        failure.getDescription().addChild(FAILED);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        Ignore ignore = description.getAnnotation(Ignore.class);
        System.out.println(ignore.value());
        testTitle(description.getDisplayName(), ColorStatus.SKIP, ignore.value());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        int testCount = failure.getDescription().testCount();
        if (testCount == 1) {
            testTitle(failure.getDescription().getMethodName(), ColorStatus.SKIP, failure.getMessage());
            failure.getDescription().addChild(SKIP);
        } else {
            mavenTitle(failure);
            List<Description> children = failure.getDescription().getChildren();
            for (Description it : children) {
                testTitle(it.getMethodName(), ColorStatus.SKIP);
            }
            mavenSkipMessage(failure);
        }

    }

    @Override
    public void testStarted(Description description) throws Exception {
    }

    @Override
    public void testFinished(Description description) throws Exception {
        if (description.getChildren().contains(FAILED)) {
        } else if (description.getChildren().contains(SKIP)) {
        } else {
            testTitle(description.getMethodName(), ColorStatus.OK);
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        //super.testRunFinished(result);
//        int count = result.getFailureCount();
//        int ignoreCount = result.getIgnoreCount();
//        boolean isSuccess = result.wasSuccessful();
//        //System.out.println("count:"+count+" ignore:"+ignoreCount+" isSuc:"+isSuccess+ " "+result.getRunCount());
//        List<Failure> fails = result.getFailures();
//        for (Failure fail : fails) {
//            //System.out.println(fail.getDescription().getClassName());
//        }
    }

}
