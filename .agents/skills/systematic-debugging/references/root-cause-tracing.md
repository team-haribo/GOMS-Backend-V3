# Root Cause Tracing Questions

- Which Service and Repository does the Controller input pass through?
- When does the transaction start and end, and when are external calls made?
- At which stage does the Redis key/TTL or JWT claim change?
- How is the exception mapped to an ErrorCode and HTTP status?
- What is the minimal input that reproduces the failure, and what is the regression test?
