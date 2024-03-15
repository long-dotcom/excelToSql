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

document.querySelector('input[type="file"]').addEventListener('change', async function (event) {
    const file = event.target.files[0];
    if (!file) return;

    // 假设后端接口URL是 '/getSheetNames'
    const formData = new FormData();
    formData.append('excelFile', file);

    const sheetNamesResponse = await fetch(`/getSheetNames`, {
        method: 'POST',
        body: formData
    });

    if (!sheetNamesResponse.ok) {
        console.error('Failed to fetch sheet names');
        return;
    }

    const sheetNames = await sheetNamesResponse.json();

    // 清空并填充Sheet名称下拉框
    const sheetNameSelect = document.getElementById('sheetNameSelect');
    sheetNameSelect.innerHTML = '';
    sheetNames.forEach(sheetName => {
        const option = document.createElement('option');
        option.value = sheetName;
        option.textContent = sheetName;
        sheetNameSelect.appendChild(option);
    });
});

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
            return response.json();  // 获取 文本内容
        })
        .then(data => {

            var decodedData = atob(data.sqlFile);
            var arrayBuffer = new ArrayBuffer(decodedData.length);
            var uint8Array = new Uint8Array(arrayBuffer);
            for (var i = 0; i < decodedData.length; i++) {
                uint8Array[i] = decodedData.charCodeAt(i);
            }
            var blob = new Blob([arrayBuffer], { type: 'application/octet-stream' });

            // 更新页面结果和下载按钮
            document.getElementById('downloadButton').style.display = 'block';
            document.getElementById('downloadButton').href = window.URL.createObjectURL(blob);
            document.getElementById('downloadButton').download = data.sqlFile; // 设置下载文件名

            // 如果需要显示预览数据
            if (data.previewData) {
                var previewData = data.previewData.join('\n\n');
                document.getElementById('result').innerText = previewData;
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
});

document.getElementById('downloadButton').addEventListener('click', function() {
    // 获取要下载的文件的 URL
    var downloadURL = this.href;

    // 创建一个链接元素
    var link = document.createElement('a');
    link.href = downloadURL;

    // 设置下载的文件名
    link.download = 'sql_file.sql'; // 设置默认文件名

    // 点击下载
    link.click();

    // 下载完成后移除链接元素
    link.remove();
});

Prism.highlightAll();





