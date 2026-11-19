# Operating System Simulator Guide

This guide explains what each simulator accepts, how it computes the result, and
how to read the output. Use **Autofill** to load a new random example into the
input tables, or enter your own values.

## 1. CPU Scheduling

### Inputs

- **Arrival time**: when a process enters the ready queue.
- **Burst time**: how long the process needs the CPU.
- **Priority**: the priority value used by Priority Scheduling. A lower value
  means a higher priority.
- **Time quantum**: the maximum CPU time given to a process in Round Robin.

### Flow

1. Start at the current time.
2. Find processes whose arrival time has been reached.
3. Select a process according to the chosen algorithm:
   - FCFS: earliest arrival.
   - SJF: shortest burst.
   - Priority: lowest priority number.
   - SRTF: shortest remaining burst.
   - Round Robin: first process in the ready queue.
4. Run the process and update its remaining burst time.
5. Continue until every process is complete.

### Formulas

```text
Turnaround = Completion - Arrival
Waiting = Turnaround - Burst
Average turnaround = total turnaround / number of processes
Average waiting = total waiting / number of processes
```

Example:

```text
Completion = 6
Arrival = 1
Burst = 3
Turnaround = 6 - 1 = 5
Waiting = 5 - 3 = 2
```

## 2. Banker's Algorithm

### Inputs

- Allocation matrix
- Maximum-demand matrix
- Available resources

Each row represents a process and each column represents a resource type.

### Flow

1. Calculate the remaining demand for every process.
2. Find a process whose remaining demand fits inside Available.
3. Let that process finish.
4. Return its allocated resources to Available.
5. Repeat until all processes finish or no process can continue.

### Formulas

```text
Need = Max - Allocation
Available after completion = Available + Allocation
```

If every process can finish, the system is in a **safe state**. If one or more
processes remain blocked, the system is **not safe**.

## 3. Deadlock Detection

Deadlock detection uses the allocation, request, and available-resource data.

### Flow

1. Check whether a process's Request fits in Available.
2. If it fits, mark the process as able to finish.
3. Return its Allocation to Available.
4. Repeat the check for the remaining processes.

```text
Request <= Available  ->  process can finish
Available after finish = Available + Allocation
```

If processes remain unable to finish, a deadlock is detected.

## 4. Paging

### Inputs

- Page size
- Number of logical pages
- Page table
- Logical address

### Flow and formulas

```text
Page number = Logical address / Page size
Offset = Logical address % Page size
Frame = Page table[Page number]
Physical address = Frame * Page size + Offset
```

If the page-table entry is negative, the page is not in memory and a page fault
is reported.

## 5. Page Replacement

For every page reference, the simulator first checks whether the page is
already in a frame.

```text
Page is present  ->  HIT
Page is absent   ->  FAULT
```

- **FIFO** replaces the oldest page.
- **LRU** replaces the least recently used page.
- **Optimal** replaces the page that will not be used for the longest time.
  Optimal gives the fewest faults, so it is the benchmark.

```text
Hit ratio = (Page hits / Total references) * 100
```

## 6. Segmentation

### Inputs

- Segment base
- Segment limit
- Segment number
- Offset

### Flow and formulas

1. Check that the selected segment exists.
2. Check that the offset is below the segment limit.
3. Calculate the physical address.

```text
Offset < Limit  ->  valid address
Offset >= Limit ->  segmentation fault
Physical address = Base + Offset
```

## 7. Fragmentation and Memory Allocation

The simulator assigns each process to a free block according to the selected
strategy:

- **First Fit**: first block large enough.
- **Best Fit**: smallest block large enough.
- **Worst Fit**: largest block large enough.

```text
Internal waste = Block size - Process size
Total internal fragmentation = sum of all internal waste
```

If enough free memory exists in total but no single block is large enough, the
result is external fragmentation.

## 8. Disk Scheduling

The simulator creates a service order based on the selected algorithm:

- FCFS
- SSTF
- SCAN
- C-SCAN

For every movement:

```text
Seek = |Current - Next|
Total head movement = sum of every seek
```

Example:

```text
Current = 53
Next = 98
Seek = |53 - 98| = 45 cylinders
```

## 9. Multiprogramming

The simulator manages CPU bursts and memory requirements at the same time.

### Flow

1. Admit processes that fit in available memory.
2. Run the first ready process for one time quantum or until it finishes.
3. Return memory when a process completes.
4. Move unfinished processes to the back of the ready queue.
5. Admit waiting processes when memory becomes available.

### Formulas

```text
CPU utilization = (Busy time / Elapsed time) * 100
Average memory utilization =
  (Memory-time / (Total memory * Elapsed time)) * 100
Peak memory utilization =
  (Peak memory used / Total memory) * 100
```

## Step-by-Step Mode

**Step by step** displays the same decisions one line at a time. It shows:

- Which process, page, or request was selected.
- Why it was selected.
- The formula used for the result.
- The updated queue, frame list, resource list, or memory state.

Use the pause button to pause the explanation and the stop button to end it.
