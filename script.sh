#!/bin/bash

# Define the container name
CONTAINER_NAME="profile-garage-1"

# Retrieve the node ID from the docker container running Garage
NODE_ID=$(docker exec -i $CONTAINER_NAME /garage status | awk '/==== HEALTHY NODES ====/ {getline; getline; print $1}')

# Check if NODE_ID is empty
if [ -z "$NODE_ID" ]; then
  echo "Error: Node ID not found."
  exit 1
fi

echo "Node ID: $NODE_ID"

# Assign layout to the node
docker exec -i profile-garage-1 /garage layout assign -z dc1 -c 1G $NODE_ID

# Apply the layout
docker exec -i profile-garage-1 /garage layout apply --version 1

# Create the bucket
docker exec -i profile-garage-1 /garage bucket create charitan-bucket

# Create the key
docker exec -i profile-garage-1 /garage key create charitan-app-key

# Grant permissions for the bucket
docker exec -i profile-garage-1 /garage bucket allow --read --write --owner charitan-bucket --key charitan-app-key

echo "Setup complete!"
