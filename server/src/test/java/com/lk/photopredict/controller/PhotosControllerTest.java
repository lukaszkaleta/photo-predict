package com.lk.photopredict.controller;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class PhotosControllerTest {

    @Test
    void uploadPhoto() throws FileNotFoundException {
        InputStream dataStream = new FileInputStream("/home/lk/test-files/x.png");
//        new PhotosController().uploadImage(dataStream);
    }
}