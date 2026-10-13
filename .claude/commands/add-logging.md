# Logging Rules (Java Spring Boot backend + React frontend)

Add logging to the code I point you at. Follow these rules. Do not change business logic, and do not add logging to files I didn't mention.

## Both sides
- Never log secrets or personal data: passwords, tokens, API keys, full request bodies, emails, phone numbers. Log IDs instead.
- Use levels properly: DEBUG for flow details, INFO for important business events, WARN for recoverable problems, ERROR for failures.
- Write messages that say what happened, with context: "Failed to load order: id=42", not "error" or "here".
- Do not add a log line to every function. Log only at entry of public service methods and API handlers (DEBUG), important business events (INFO), and failures (ERROR). Skip getters, setters, small helpers and loops.
- Log each failure once. Do not log an exception and then rethrow it at every layer.
- Never leave an empty catch block.

## Java (Spring Boot)
- Use SLF4J. Use Lombok `@Slf4j` if the project already uses Lombok; otherwise `private static final Logger log = LoggerFactory.getLogger(X.class);`
- Use parameterized messages: `log.debug("id={}", id)`, never string concatenation.
- Pass the exception as the LAST argument so the stack trace is logged: `log.error("Failed to load order: id={}", id, e)`.
- Catch specific exceptions, not `Exception`. Log in the catch block only if you handle the error or wrap it. Expected cases (like "not found") do not need ERROR logs.
- Unexpected exceptions that reach the top are logged once in the `@ControllerAdvice` handler.
- Do not use `System.out.println` or `e.printStackTrace()`.

```java
@Slf4j
@Service
public class OrderService {

    public Order getOrder(Long id) {
        log.debug("getOrder called: id={}", id);
        try {
            return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id));
        } catch (DataAccessException e) {
            log.error("Failed to load order: id={}", id, e);
            throw new ServiceException("Could not load order", e);
        }
    }
}
```

## React
- Do not use `console.log` directly. Use a small logger utility (`src/utils/logger.js`) so debug output is off in production.
- Log in these places only: failed API calls, error boundaries, and important user actions (INFO). Do not log inside render or on every re-render.
- In every `catch` for an API call, log the error with context, then show the user an error state.
- Add an error boundary at the app root that logs with `componentDidCatch`.
- In production, send ERROR logs to the monitoring tool (e.g. Sentry) if the project uses one.

```js
// src/utils/logger.js
const isDev = import.meta.env.DEV; // CRA: process.env.NODE_ENV !== 'production'

export const logger = {
  debug: (...args) => isDev && console.debug('[debug]', ...args),
  info:  (...args) => isDev && console.info('[info]', ...args),
  warn:  (...args) => console.warn('[warn]', ...args),
  error: (msg, err, ctx) => {
    console.error('[error]', msg, err, ctx);
    // send to monitoring here, e.g. Sentry.captureException(err)
  },
};
```

```jsx
async function loadOrder(id) {
  try {
    const res = await api.get(`/orders/${id}`);
    setOrder(res.data);
  } catch (err) {
    logger.error('Failed to fetch order', err, { id });
    setError('Could not load the order. Please try again.');
  }
}
```

## Before you finish
- Confirm the code still compiles or builds.
- List the files you changed and anything you intentionally left unlogged.