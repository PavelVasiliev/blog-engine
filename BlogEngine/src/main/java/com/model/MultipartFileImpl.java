package com.model;

import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor
public class MultipartFileImpl implements MultipartFile {

    private MultipartFile multipartFile;
    private String name;

    public MultipartFileImpl(MultipartFile mf) {
        multipartFile = mf;
    }

    public MultipartFileImpl(String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return multipartFile.getName();
    }

    @Override
    public String getOriginalFilename() {
        return multipartFile.getOriginalFilename();
    }

    @Override
    public String getContentType() {
        return multipartFile.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return multipartFile.isEmpty();
    }

    @Override
    public long getSize() {
        return multipartFile.getSize();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return multipartFile.getBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }

    @Override
    public void transferTo(File file) throws IOException, IllegalStateException {
        multipartFile.transferTo(file);
    }
}
