import {Loading, Message} from 'element-ui'
import { saveAs } from 'file-saver'
import errorCode from '@/utils/errorCode'

let downloadLoadingInstance;

export default {
  zip(response) {
    downloadLoadingInstance = Loading.service({ text: "正在下载数据，请稍候", spinner: "el-icon-loading", background: "rgba(0, 0, 0, 0.7)", })
    response.then((res) => {
      const isBlob = res.headers.getContentType().includes("stream")
      if (isBlob) {
        // 二进制数据
        const blob = new Blob([res.data], { type: 'application/zip' })

        // 文件名
        var regex = /filename="(.*?)"/;
        var match = regex.exec(res.headers.get('Content-Disposition'));
        var filename = match[1];
        // 生成文件
        this.saveAs(blob, filename)
      } else {
        this.printErrMsg(res.data);
      }
      downloadLoadingInstance.close();
    }).catch((r) => {
      console.error(r)
      Message.error('下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close();
    })
  },
  saveAs(text, name, opts) {
    saveAs(text, name, opts);
  },
  async printErrMsg(data) {
    const resText = await data.text();
    const rspObj = JSON.parse(resText);
    const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
    Message.error(errMsg);
  }
}
