package model

import "net/http"

// 项目统一返回结构
type JsonResult[T any] struct {
	Success bool `json:"success"` // 是否成功 true or false

	Code int `json:"code"` // 状态码

	Msg string `json:"msg"` // 返回内容

	Data T `json:"data"` // 数据对象
}

// Success 初始化一个新创建的 AjaxResult 对象
// @param type 状态类型
// @param msg 返回内容
// @param data 数据对象
func Success[T any](data T) *JsonResult[T] {
	return &JsonResult[T]{
		Success: true,
		Code:    http.StatusOK,
		Msg:     "操作成功",
		Data:    data,
	}
}

// Error 返回错误消息
// @param msg 返回内容
func Error(msg string) *JsonResult[string] {
	return &JsonResult[string]{
		Success: true,
		Code:    http.StatusOK,
		Msg:     msg,
		Data:    "",
	}
}
