## Replication Filter

1) Start sync gateway

`````bash
$ ~/Downloads/sync_gateway/bin/sync_gateway syncgateway-config.json
```

2) Start the app in the android emulator. If you're using a genymotion emulator, change the [hostname](https://github.com/Jamiltz/ReplicationFilter/blob/master/app/src/main/java/android/jamiltz/com/replicationfilter/Application.java#L26).

### Three methods to test filtered replication

#### Filter function without params

Call `startPushWithFilterFunc` in `onCreate` of `Application.java`.

Documents with an even \_id property are deleted and won't be replicated to Sync Gateway.

You can see that in the Sync Gateway logs:

![logs](http://f.cl.ly/items/1H30401i0f1S0S3P1T0m/Screen%20Shot%202015-02-06%20at%2016.31.13.png)

#### Filter function with params

Call `startPushWithFilterFuncWithParams`.

Only docs with the `name` property set to `Waldo` should be replicated.

#### Array of document IDs

Call `startPushWithArrayOfIds`.

Only docs with the specified ids should be replicated.

