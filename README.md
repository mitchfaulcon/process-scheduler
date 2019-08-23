# process-scheduler
A Process Scheduler application for Team 12's SOFTENG 306 Project 1

## Usernames
| Name | GitHub Username  | UoA Username | UoA ID |
| ---- | ---------------- | ------------ | ------ |
| Max Benson | veratrum | mben346 | 655363680 |
| Dhruv Phadnis | mini | dpha900 | 902553757 |
| Brad Coleman | bradleycoleman | bcol085 | 223284129 |
| Eric Pedrido | EricPedrido | eped596 | 467829287 |
| Mitchell Faulconbridge | mitchfaulcon | mfau584 | 250334267 |

## Project Overview
Process Scheduler takes in a graph file in DOT format (.dot) and outputs an optimal schedule in another DOT file based on how many processors the user wants the tasks to be scheduled on.

## Run Instructions
1. Download the <>.jar file  
2. Navigate to the directory containing the jar file and run it using:  
`java -jar <>.jar INPUT.dot P [OPTIONAL]`  
Where INPUT.dot is the graph input file, P is the number of processors to calculate the optimal schedule on, and \[OPTIONAL] is any optional arguments.  
These optional arguments are:  
* `-p N` How many cores to use use for parallel execution (default is 1)  
* `-v` Enables a GUI visualisation of the schedule search with graphs and statistics  
* `-o Output` The output DOT file will be named OUTPUT.dot (default is INPUT-output.dot)

## Other Info
* Our team meeting minutes can be found on the meetings [wiki page](https://github.com/mitchfaulcon/process-scheduler/wiki/Meetings).  
* A copy of our other files (planning, presentations, etc.) can be found [here](https://drive.google.com/drive/folders/13yAeFWhUZUvv9ybfBkAa7boKlnE2WNVv?usp=sharing).  
* Older versions of the application can be found on the [releases](https://github.com/mitchfaulcon/process-scheduler/releases) page.
