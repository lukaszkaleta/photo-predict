package com.aceve.hackathon.repository;

public interface Bucket {

    String name();

    String contentType();

    String root();

    enum Name implements Bucket {
        Image("h2025-images", "image/jpeg"),
        Recording("h2025-records", "audio/mpeg"),
        Deviation("h2025-deviations", "application/json");

        private final String root;
        private final String contentType;

        Name(String root, String contentType) {
            this.root = root;
            this.contentType = contentType;
        }

        public String root() {
            return root;
        }
        public String contentType() {
            return contentType;
        }

        public String gcsPath() {
            return "gs://" + root();
        }
        public String gcsPath(String id) {
            return gcsPath() + "/" + id;
        }
    }
}
