## Camel Data Provider

[Camel](http://camel.apache.org/) component to plug in any data provider as *consumer* (specified with
**from** when starting a route).

`DataProviderConsumer` extends `ScheduledBatchPollingConsumer` meaning that it will be regularly polled
for new data.

The first step to use this component is to extend `IDataProvider` or one of its extensions:

1. `StaticDataProvider`: the data used are already known and could be specified in the constructor.
1. `LazyDataProvider`: `loadData` invocation happens when data are needed for the first time. This is useful when,
 during route definition, data are not available yet. Or when the route is later started, when the data are available.

### Example

```
from("dataprovider://myDataProvider?consumer.useFixedDelay=true&consumer.maxMessagesPerPoll=20&initialDelay=20")
.to("mock:direct:end");
```
