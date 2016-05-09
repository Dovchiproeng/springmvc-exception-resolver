<html>
<body>
<h2>Test exception response</h2>
<a href="api/demo/v1/customers"> Get all customers</a><br>
<a href="api/demo/v1/customers/search?name=Vandet PIN"> Search name</a><br>
<a href="api/demo/v1/customers/search"> Parameter required error</a><br>
<a href="api/demo/v1/customers/search?name=java"> search name not found</a><br>
<a href="api/demo/v1/customers/search?name=2323"> name can not be number error</a><br>
<a href="api/demo/v1/customers/search?name=error"> get business exception</a><br>
</body>
</html>
