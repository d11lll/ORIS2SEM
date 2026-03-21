<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Список лиц</title>
</head>
<body>

<h2>Список всех лиц</h2>

<table border="1">
    <tr>
        <th>Тип</th>
        <th>Имя</th>
        <th>Телефон</th>
        <th>Доп. информация</th>
    </tr>
    <#list persons as person>
        <tr>
            <td>${person.getClass().getSimpleName()}</td>
            <td>${person.name}</td>
            <td>${person.phone.number}</td>
            <td>
                <#if person.getClass().getSimpleName() == "Client">
                    ${person.address!}
                <#elseif person.getClass().getSimpleName() == "Admin">
                    ${person.email!}
                </#if>
            </td>
        </tr>
    </#list>
</table>

<br/>
<a href="/person/add">Добавить лицо</a> |
<a href="/">На главную</a>

</body>
</html>