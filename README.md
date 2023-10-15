# fantastic-rotary

# IN5020 Assignment 2 - Distributed Bank Account with Spread Toolkit
## Autumn 2023 Group G7

The code for this project is available on [https://github.com/chrfrenning/fantastic-rotary](https://github.com/chrfrenning/fantastic-rotary).

# Differences between the two getSyncedBalance approaches.

## Naive approach
In the naive approach, the replica receiving the `getSyncedBalance` will wait until all the outstanding transactions have been applied, and then respond with the balance to the user. This approach is not reliable, as the response will be incorrect when later balance updates will be enqueued and executed while the replica is waiting for the empty queue. In addition, the system may never process all the transactions in the queue, and new transactions will arrive, which can cause infinite waiting. 

## Ordered approach
In the ordered approach, the replica will treat the incoming `getSyncedBalance` request as a transaction. It means that upon receiving the request, the replica will create a transaction, and put it in the queue of outstanding transactions, which will be multicasted to the group. When the replica gets the same transaction from Spread multicast, it'll know that all the replicas are in sync and show the balance to the user in the right order.


# How to run

Start on the command line, you must manually start minimum the correct number of replicas:

```
    java -jar Replica.jar 
      [--server <server_ip>]
      [--port <port>]
      [--account <account_name>]
      [--replicas 3]
      [--name <replica_instance_name>]
      [--file <input.dat]
```

All parameters are optional. Defaults are IFI Spread Servers and interactive mode.

## Setup details
For developers (if you clone this project from github)

### Running spread

Spread may be running, otherwise you can start it with the following command:

```spread -c ./spread.conf```


### Running the program (for developers)

Use `./run.sh` to compile and run the java app.
