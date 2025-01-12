#!/bin/bash

# Define the container name
CONTAINER_NAME="profile-garage"

# Retrieve the node ID from the docker container running Garage
NODE_ID=$(docker exec -i $CONTAINER_NAME /garage status | awk '/==== HEALTHY NODES ====/ {getline; getline; print $1}')

# Check if NODE_ID is empty
if [ -z "$NODE_ID" ]; then
  echo "Error: Node ID not found."
  exit 1
fi

echo "Node ID: $NODE_ID"

# Check if any keys already exist
KEY_LIST=$(docker exec -i $CONTAINER_NAME /garage key list | tail -n +2) # Skip the header row
if [ -n "$KEY_LIST" ]; then
  echo "Keys already exist. Skipping setup."
  exit 0
fi

echo "No keys found. Proceeding with setup."

# Assign layout to the node
docker exec -i $CONTAINER_NAME /garage layout assign -z dc1 -c 1G $NODE_ID

# Apply the layout
docker exec -i $CONTAINER_NAME /garage layout apply --version 1

# Create the bucket
docker exec -i $CONTAINER_NAME /garage bucket create charitan-bucket

# Create the key
docker exec -i $CONTAINER_NAME /garage key create charitan-app-key

# Grant permissions for the bucket
docker exec -i $CONTAINER_NAME /garage bucket allow --read --write --owner charitan-bucket --key charitan-app-key

echo "Setup complete!"
