-- Create document_metadata table for DocumentMetadata entity.
-- Uses uuid-ossp extension (already created by V1) for UUID generation.

CREATE TABLE IF NOT EXISTS document_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_name VARCHAR(255),
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    total_pages INTEGER,
    total_chunks INTEGER,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
