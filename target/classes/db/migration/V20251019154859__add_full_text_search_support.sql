-- Create a function to generate search vectors
CREATE OR REPLACE FUNCTION generate_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    -- Update the search_vector column with the concatenated text from searchable fields
    -- Adjust the table and column names based on your actual schema
    NEW.search_vector = 
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.content, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Example for a documents table (modify as needed for your tables)
-- Create a search_vector column if it doesn't exist
ALTER TABLE documents ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Create a trigger to update the search_vector column
CREATE TRIGGER documents_search_vector_update
BEFORE INSERT OR UPDATE ON documents
FOR EACH ROW EXECUTE FUNCTION generate_search_vector();

-- Create a GIN index for fast searching
CREATE INDEX IF NOT EXISTS documents_search_vector_idx ON documents USING GIN(search_vector);

-- Create a function for searching
CREATE OR REPLACE FUNCTION search_documents(query_text text)
RETURNS TABLE(id bigint, title text, content text, rank float) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.id,
        d.title,
        d.content,
        ts_rank(d.search_vector, plainto_tsquery('english', query_text)) as rank
    FROM documents d
    WHERE d.search_vector @@ plainto_tsquery('english', query_text)
    ORDER BY rank DESC;
END;
$$ LANGUAGE plpgsql;
