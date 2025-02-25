# Verify vespa-cli install
vespa version

# intall python dependencies
poetry install
source $(poetry env info -p)/bin/activate

# Download MIND dataset
chmod +x bin/download-mind.sh
./bin/download-mind.sh demo
poetry run python3 convert_to_vespa_format.py mind/

# Run vespa app
docker-compose up --detach vespa

# Build application package
cd my-app && mvn clean generate-resources compile test package

vespa deploy --wait 300 my-app

# Feed data
vespa feed mind/vespa.json

# Test with query
vespa query "query=test" | jq