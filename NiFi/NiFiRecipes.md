# NiFi Recipes



## Kill NiFi Process in Windows

Find the PID:

```bash
C:\app\nifi\run>cd nifi\run
C:\app\nifi\run>type nifi.pid
7180
# This nieeds to be executed as administrator
C:\app\nifi\run>taskkill /PID 7180 /F
```

