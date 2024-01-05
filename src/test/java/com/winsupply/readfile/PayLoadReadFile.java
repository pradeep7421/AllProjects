package com.winsupply.readfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.util.ResourceUtils;

public class PayLoadReadFile {

    public static String readFile(final String pFilePath) throws IOException {
        final File lFile = ResourceUtils.getFile("classpath:" + pFilePath);
        final String lContent = new String(Files.readAllBytes(lFile.toPath()));
        return lContent;
    }
}
