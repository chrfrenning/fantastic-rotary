# fantastic-rotary

IN5020 Assignment 2 - Distributed Bank Account with Spread Toolkit


# How to run

Start on the command line, you must manually start minimum the correct number of replicas:

```
    java -jar Replica.jar 
      [--server <server_ip>]
      [--port <port>]
      [--account <account_name>]
      [--replicas 3]
      [--file <input.dat]
```

All parameters are optional. Defaults are IFI Spread Servers and interactive mode.


## For developers (if you clone this project from github)

### Running spread

Spread may be running, otherwise you can start it with the following command:

```spread -c ./spread.conf```


### Running the program (for developers)

Use `./run.sh` to compile and run the java app.