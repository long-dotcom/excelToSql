function toggleFields() {
    var typeSelect = document.getElementsByName('type')[0];
    var startIndexLabel = document.querySelector('label[for="startIndex"]');
    var startIndexInput = document.getElementsByName('startIndex')[0];
    var endIndexLabel = document.querySelector('label[for="endIndex"]');
    var endIndexInput = document.getElementsByName('endIndex')[0];
    var conditionNumsLabel = document.querySelector('label[for="conditionNums"]');
    var conditionNumsInput = document.getElementsByName('conditionNums')[0];

    if (typeSelect.value === 'add') {
        startIndexLabel.classList.add('hidden');
        startIndexInput.classList.add('hidden');
        endIndexLabel.classList.add('hidden');
        endIndexInput.classList.add('hidden');
        conditionNumsLabel.classList.add('hidden');
        conditionNumsInput.classList.add('hidden');
        // 清空相关字段的值
        startIndexInput.value = '';
        endIndexInput.value = '';
        conditionNumsInput.value = '';
    } else {
        startIndexLabel.classList.remove('hidden');
        startIndexInput.classList.remove('hidden');
        endIndexLabel.classList.remove('hidden');
        endIndexInput.classList.remove('hidden');
        conditionNumsLabel.classList.remove('hidden');
        conditionNumsInput.classList.remove('hidden');
    }
}

document.getElementById('excelToSqlForm').addEventListener('submit', function(e) {
    e.preventDefault();

    // 获取表单数据
    var formData = new FormData(this);


    // 在提交前检查 startIndex 和 endIndex 是否为空，如果为空则设置默认值
    if (formData.get('startIndex') === null || formData.get('startIndex') === "") {
        formData.set('startIndex', '0'); // 设置一个默认值
    }

    if (formData.get('endIndex') === null || formData.get('endIndex') === "") {
        formData.set('endIndex', '0'); // 设置一个默认值
    }
    // 发送 fetch 请求
    fetch('/excelToSqlFile', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();  // 获取 文本内容
        })
        .then(textContent => {
            // 显示结果在页面
            document.getElementById('result').innerText = "";
            // 显示下载按钮
            document.getElementById('downloadButton').style.display = 'block';

        })
        .catch(error => {
            console.error('Error:', error);
        });
});


// 下载结果按钮的点击事件
$('#downloadButton').click(function(e) {
    e.preventDefault();  // 阻止默认行为

    // 获取结果内容
    var resultContent = $('#result').text();

    // 创建 Blob 对象
    var blob = new Blob([resultContent], { type: 'text/plain' });

    // 创建下载链接
    var downloadLink = document.createElement('a');
    downloadLink.href = window.URL.createObjectURL(blob);
    downloadLink.download = 'output.txt';

    // 将链接添加到页面，并模拟点击下载
    document.body.appendChild(downloadLink);
    downloadLink.click();

    // 移除链接
    document.body.removeChild(downloadLink);
});
