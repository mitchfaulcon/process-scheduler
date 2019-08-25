#!/usr/bin/bash
# before this script will work, you need to export the project as a runnable .jar file to bin/process-scheduler.jar
# 
# example output (lab computers):
#
# mben346@en-369809:~/Desktop/306a1/process-scheduler$ . ./time.sh
# Sequential BNB:
# Nodes_8, 10: 0.00 seconds
# Nodes_9, 5: 0.06 seconds
# Nodes_10, 20: 0.13 seconds
# Nodes_11, 2: 6.23 seconds
# Parallel BNB:
# Nodes_8, 10: 0.01 seconds
# Nodes_9, 5: 0.06 seconds
# Nodes_10, 20: 0.15 seconds
# Nodes_11, 2: 2.99 seconds

echo "Sequential BNB:"
echo "Nodes_8, 10: $(java -jar bin/process-scheduler.jar test_data/Nodes_8_Random.dot 10 -p 1 | grep -Po '[\d]+\.\d\d seconds')"
echo "Nodes_9, 5: $(java -jar bin/process-scheduler.jar test_data/Nodes_9_SeriesParallel.dot 5 -p 1 | grep -Po '[\d]+\.\d\d seconds')"
echo "Nodes_10, 20: $(java -jar bin/process-scheduler.jar test_data/Nodes_10_Random.dot 20 -p 1 | grep -Po '[\d]+\.\d\d seconds')"
echo "Nodes_11, 2: $(java -jar bin/process-scheduler.jar test_data/Nodes_11_OutTree.dot 2 -p 1 | grep -Po '[\d]+\.\d\d seconds')"

echo "Parallel BNB:"
echo "Nodes_8, 10: $(java -jar bin/process-scheduler.jar test_data/Nodes_8_Random.dot 10 -p 4 | grep -Po '[\d]+\.\d\d seconds')"
echo "Nodes_9, 5: $(java -jar bin/process-scheduler.jar test_data/Nodes_9_SeriesParallel.dot 5 -p 4 | grep -Po '[\d]+\.\d\d seconds')"
echo "Nodes_10, 20: $(java -jar bin/process-scheduler.jar test_data/Nodes_10_Random.dot 20 -p 4 | grep -Po '[\d]+\.\d\d seconds')"
echo "Nodes_11, 2: $(java -jar bin/process-scheduler.jar test_data/Nodes_11_OutTree.dot 2 -p 4 | grep -Po '[\d]+\.\d\d seconds')"
