<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Добавить лицо</title>
</head>
<body>

<h2>Добавление лица</h2>

<form method="post">
    <div>
        <label>Тип:</label><br/>
        <input type="radio" name="type" value="client" checked/> Клиент
        <input type="radio" name="type" value="admin"/> Администратор
    </div>

    <div>
        <label>Имя:</label><br/>
        <input type="text" name="name" required/>
    </div>

    <div>
        <label>Номер телефона:</label><br/>
        <input type="text" name="phone" required/>
    </div>

    <div id="addressDiv">
        <label>Адрес:</label><br/>
        <input type="text" name="extra"/>
    </div>

    <div id="emailDiv" style="display:none">
        <label>Email:</label><br/>
        <input type="email" name="extra"/>
    </div>

    <br/>
    <button type="submit">Сохранить</button>
</form>

<br/>
<a href="/">На главную</a>

<script>
    const radios = document.querySelectorAll('input[name="type"]');
    radios.forEach(radio => {
        radio.addEventListener('change', function() {
            const addressDiv = document.getElementById('addressDiv');
            const emailDiv = document.getElementById('emailDiv');
            if (this.value === 'client') {
                addressDiv.style.display = 'block';
                emailDiv.style.display = 'none';
            } else {
                addressDiv.style.display = 'none';
                emailDiv.style.display = 'block';
            }
        });
    });
</script>

</body>
</html>