package model

import (
	"errors"
	"reflect"
)

var (
	ErrInvalidDst = errors.New("dest must be a pointer to struct")
	ErrInvalidSrc = errors.New("src must be struct or pointer to struct")
)

// CopyAttribute copies fields with the same name between structs.
// 不同结构体之间按字段名复制值
// 字段类型必须兼容（相同或可赋值）
// 支持匿名结构体嵌入（embedded struct）
// 仅复制可导出字段（Go 反射限制）
// 可选忽略零值、字段过滤等功能
func CopyAttribute(dst any, src any) error {
	dstVal := reflect.ValueOf(dst)
	if dstVal.Kind() != reflect.Pointer || dstVal.Elem().Kind() != reflect.Struct {
		return ErrInvalidDst
	}
	dstVal = dstVal.Elem()

	srcVal := reflect.ValueOf(src)
	if srcVal.Kind() == reflect.Pointer {
		srcVal = srcVal.Elem()
	}
	if srcVal.Kind() != reflect.Struct {
		return ErrInvalidSrc
	}

	dstFields := flatten(dstVal)
	srcFields := flatten(srcVal)

	for name, dstf := range dstFields {
		if srcf, ok := srcFields[name]; ok {
			// 类型兼容检查
			if srcf.Type().AssignableTo(dstf.Type()) && dstf.CanSet() {
				dstf.Set(srcf)
			}
		}
	}

	return nil
}

// flatten extracts all exported fields, including anonymous embedded ones.
func flatten(v reflect.Value) map[string]reflect.Value {
	result := make(map[string]reflect.Value)
	t := v.Type()

	for i := 0; i < t.NumField(); i++ {
		sf := t.Field(i)
		fv := v.Field(i)

		// 跳过不可导出的字段
		if !sf.IsExported() {
			continue
		}

		// 匿名嵌入结构体 -> 递归
		if sf.Anonymous && fv.Kind() == reflect.Struct {
			for k, v2 := range flatten(fv) {
				result[k] = v2
			}
		} else {
			result[sf.Name] = fv
		}
	}

	return result
}
