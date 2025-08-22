package com.aceve.hackathon.analysis;

import com.aceve.hackathon.HackathonApplication;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.UUID;


/**
 * Vision API
 * <a href="https://cloud.google.com/vision/product-search/docs/searching">...</a>
 * this api is about creation own products base and operate on this base with searching, detection and get similarity
 */
@ContextConfiguration(classes = { HackathonApplication.class })
class ImageCaptioningTest {

    @Autowired
    private CredentialsProvider credentialsProvider;

    @Test
    void name() {
//        new ImageCaptioning().process("");
    }

    @Test
    void createProductTest() throws IOException {
        String productId = UUID.randomUUID().toString();
        new ImageCaptioning(credentialsProvider).createProduct(productId, "Test displayName " + productId, "Test description " + productId, "homegoods");
//        projects/hackathon2025-458305/locations/europe-west1/products/d9642f93-6f43-45c5-a7d2-4f2bb4c2a749
    }

    @Test
    void addProductImageTest() throws IOException {
        new ImageCaptioning(credentialsProvider).addProductImage("d9642f93-6f43-45c5-a7d2-4f2bb4c2a749", "db96297f-2d4b-4228-9522-587fa44c6f58");
//        projects/hackathon2025-458305/locations/europe-west1/products/d9642f93-6f43-45c5-a7d2-4f2bb4c2a749/referenceImages/db96297f-2d4b-4228-9522-587fa44c6f58
    }

    @Test
    void listProductsTest() throws IOException {
        new ImageCaptioning(credentialsProvider).listProducts();
    }

    @Test
    void getProductsInfoTest() throws IOException {
        String id = "db96297f-2d4b-4228-9522-587fa44c6f58"; // ruler - not ok
//        String id = "395c738d-c630-46e0-8e55-7225b1a22365"; // Apple logo - ok
        new ImageCaptioning(credentialsProvider).getInfo(id);
    }
}
