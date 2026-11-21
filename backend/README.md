## check if table is created
docker exec -it expense-postgres psql -U postgres -d expense_db -c "SELECT * FROM expenses;"

## upload file
curl.exe -F "file=@C:\шлях\до\вашого\файлу.pdf" http://localhost:8080/api/expenses/upload-pdf
