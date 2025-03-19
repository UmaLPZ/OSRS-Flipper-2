//package com.flipper2;
//
//import java.io.File;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import com.flipper2.helpers.Persistor;
//
//public class TestUtilities {
//    public static void cleanTestResultFiles() {
//        Path currentRelativePath = Paths.get("");
//        String testFilePath = currentRelativePath.toAbsolutePath().toString()
//                + "\\src\\test\\java\\com\\flipper2\\test-result-files";
//        // Delete any generated test-result-files
//        File deleteBuys = new File(testFilePath + "\\" + Persistor.BUYS_JSON_FILE);
//        File deleteSells = new File(testFilePath + "\\" + Persistor.SELLS_JSON_FILE);
//        deleteBuys.delete();
//        deleteSells.delete();
//
//    }
//
//}