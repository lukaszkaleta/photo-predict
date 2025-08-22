package com.aceve.hackathon.controller;

import com.aceve.hackathon.repository.DataStore;
import com.aceve.hackathon.HackathonApplication;
import com.google.api.gax.core.CredentialsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
class RecordsControllerTest {


    @Autowired @Qualifier("recording")
    private DataStore recordind;

    @Autowired
    private RecordsController recordsController;

    @Test
    void transcription() {
        recordsController.transcription("2880ae88-5f05-463e-9fb3-34fc966ab911");
    }
}