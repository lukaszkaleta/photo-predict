package com.lk.photopredict.analysis;

import com.lk.photopredict.repository.Bucket;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.ProductSearchClient;
import com.google.cloud.vision.v1.Product;
import com.google.cloud.vision.v1.Product.KeyValue;
import com.google.cloud.vision.v1.ProductName;
import com.google.cloud.vision.v1.ReferenceImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lk.photopredict.HackathonApplication.LOCATION;
import static com.lk.photopredict.HackathonApplication.PROJECT_ID;

public class ImageCaptioning {

    private final CredentialsProvider credentialsProvider;

    public ImageCaptioning(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    // Create the image source from GCS bucket
    private ImageSource imageSource(String id) {
        String gcsImageUri = Bucket.Name.Image.gcsPath(id);
        return ImageSource.newBuilder().setGcsImageUri(gcsImageUri).build();
    }

    private Image image(String id) {
        return Image.newBuilder().setSource(imageSource(id)).build();
    }

    public String process(String id) throws IOException {
        ImageAnnotatorSettings vision = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        ImageAnnotatorClient client;
        try {
            client = ImageAnnotatorClient.create(vision);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image image = this.image(id);

        // Create the feature object
        Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).setMaxResults(2).build();

        // Create the request object
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(image).build();

        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        // Perform the request
        AnnotateImageResponse response = client.batchAnnotateImages(requests).getResponses(0);

        // Print the captions/labels
        response.getLabelAnnotationsList().forEach(label -> System.out.println(label.getDescription()));

        return "";
    }


    public String getInfo(String id) throws IOException {
        ImageAnnotatorSettings builder = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(builder)) {
            // Load image from GCS
            String gcsImageUri = Bucket.Name.Image.gcsPath(id);
            ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsImageUri).build();
            Image image = Image.newBuilder().setSource(imgSource).build();

            // Create feature for product search
            Feature productFeature = Feature.newBuilder()
                    .setType(Feature.Type.PRODUCT_SEARCH)
                    .setMaxResults(5)
                    .build();

            // Create feature for logo detection
            Feature logoFeature = Feature.newBuilder()
                    .setType(Feature.Type.LOGO_DETECTION)
                    .setMaxResults(5)
                    .build();

            // Create feature for logo detection
            Feature webDetection = Feature.newBuilder()
                    .setType(Type.WEB_DETECTION)
                    .setMaxResults(5)
                    .build();

            // Create request with both features
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(webDetection)
                    .addFeatures(productFeature)
                    .addFeatures(logoFeature)
                    .setImage(image)
                    .build();

            List<AnnotateImageResponse> responses = vision.batchAnnotateImages(List.of(request)).getResponsesList();

            // Process Web detection results
            System.out.println("Web detection:");
            responses.forEach(response -> {
                if (response.hasError()) {
                    System.out.println("Error: " + response.getError().getMessage());
                }
                response.getWebDetection().getWebEntitiesList().stream()
                        .limit(5)
                        .forEach(entity -> {
                            System.out.println(entity.getDescription());
                            System.out.println(entity.getScore());
                        });
            });


            // Process logo detection results
            System.out.println("\nLogos detected:");
            responses.forEach(response -> {
                if (response.hasError()) {
                    System.out.println("Error: " + response.getError().getMessage());
                }
                response.getLogoAnnotationsList().forEach(logo ->
                        System.out.println("- " + logo.getDescription())
                );
            });

            // Process product detection results
            System.out.println("\nProducts detected:");
            responses.forEach(response -> {
                if (response.hasError()) {
                    System.out.println("Error: " + response.getError().getMessage());
                }
                if (response.hasProductSearchResults()) {
                    ProductSearchResults results = response.getProductSearchResults();
                    System.out.println("Index time: " + results.getIndexTime());
                    System.out.println("Results count: " + results.getResultsCount());

                    results.getResultsList().forEach(result -> {
                        Product product = result.getProduct();
                        System.out.println("- Product: " + product.getDisplayName());
                        System.out.println("  Description: " + product.getDescription());
                        System.out.println("  Category: " + product.getProductCategory());
                        System.out.println("  Score: " + result.getScore());
                    });
                } else {
                    System.out.println("No product search results found");
                }
            });

            return "";
        }
    }

    public void createProduct(String productId, String displayName, String description, String category) throws IOException {
        ProductSearchSettings settings = ProductSearchSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (ProductSearchClient client = ProductSearchClient.create(settings)) {
            // Create the product name
            String formattedParent = String.format("projects/%s/locations/%s", PROJECT_ID, LOCATION);

            // Create the product
            Product product = Product.newBuilder()
                    .setName(ProductName.of(PROJECT_ID, LOCATION, productId).toString())
                    .setDisplayName(displayName)
                    .setDescription(description)
                    .setProductCategory(category)
                    .addProductLabels(KeyValue.newBuilder()
                            .setKey("brand")
                            .setValue("Hackathon2025")
                            .build())
                    .build();

            // Create the product in the dataset
            Product createdProduct = client.createProduct(formattedParent, product, productId);
            System.out.println("Product created successfully:");
            System.out.println("- Name: " + createdProduct.getName());
            System.out.println("- Display Name: " + createdProduct.getDisplayName());
            System.out.println("- Category: " + createdProduct.getProductCategory());
        }
    }

    public void addProductImage(String productId, String imageId) throws IOException {
        String gcsUri = "gs://h2025-images/" + imageId;
        ProductSearchSettings settings = ProductSearchSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (ProductSearchClient client = ProductSearchClient.create(settings)) {
            // Create the product name
            String productName = ProductName.of(PROJECT_ID, LOCATION, productId).toString();

            // Create the reference image
            ReferenceImage referenceImage = ReferenceImage.newBuilder()
                    .setUri(gcsUri)
                    .build();

            // Add the image to the product
            ReferenceImage createdImage = client.createReferenceImage(productName, referenceImage, imageId);
            System.out.println("Image added successfully:");
            System.out.println("- Image URI: " + createdImage.getUri());
            System.out.println("- Image Name: " + createdImage.getName());
        }
    }

    public List<Product> listProducts() throws IOException {
        ProductSearchSettings settings = ProductSearchSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (ProductSearchClient client = ProductSearchClient.create(settings)) {
            String formattedParent = String.format("projects/%s/locations/%s", PROJECT_ID, LOCATION);
            List<Product> products = new ArrayList<>();

            for (Product product : client.listProducts(formattedParent).iterateAll()) {
                products.add(product);
                System.out.println("Product: " + product.getDisplayName() +
                        " (ID: " + product.getName() + ")");
            }

            return products;
        }
    }


}