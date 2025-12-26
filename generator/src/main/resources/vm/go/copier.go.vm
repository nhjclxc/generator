package copier

import (
	"errors"
	"reflect"
	"strings"
	"sync"
)

var (
	ErrInvalidDst = errors.New("dest must be a pointer to struct")
	ErrInvalidSrc = errors.New("src must be struct or pointer to struct")
)

var fieldCache sync.Map // map[reflect.Type]map[string]int

// CopyOptions 属性复制可选操作方法
type CopyOptions struct {
	IgnoreZero bool
	Fields     map[string]struct{}
	Exclude    map[string]struct{}
	Tag        string
}

// fieldInfo 当结构体被匿名嵌套多层时，用于标记当前拷贝的层级
type fieldInfo struct {
	index []int
}

type CopyOption func(*CopyOptions)

// WithIgnoreZero 跳过零值字段，避免覆盖已有值
func WithIgnoreZero() CopyOption {
	return func(o *CopyOptions) {
		o.IgnoreZero = true
	}
}

// WithTag 用 struct tag 做字段名映射 ，如 WithTag("json")表示用json tag来作为复制标识
func WithTag(tag string) CopyOption {
	return func(o *CopyOptions) {
		o.Tag = tag
	}
}

// WithFields 只拷贝指定字段（白名单），如 WithFields("ID", "Name")表示只复制这两个字段的值
func WithFields(fields ...string) CopyOption {
	return func(o *CopyOptions) {
		o.Fields = make(map[string]struct{}, len(fields))
		for _, f := range fields {
			o.Fields[f] = struct{}{}
		}
	}
}

// WithExclude 排除指定字段（黑名单），如WithExclude("Password", "CreatedAt")表示这两个字段不复制
func WithExclude(fields ...string) CopyOption {
	return func(o *CopyOptions) {
		o.Exclude = make(map[string]struct{}, len(fields))
		for _, f := range fields {
			o.Exclude[f] = struct{}{}
		}
	}
}

// CopyAttribute 一个 高可控、可配置、支持类型转换与深拷贝的 Go 结构体复制 / 合并工具
// 1️⃣ 同名字段自动拷贝
// 2️⃣ tag 映射（字段名不一致也能拷）
// 3️⃣ 匿名嵌入结构体（embedded struct）
// 4️⃣ 类型自动转换（非常核心）
// 5️⃣ 深拷贝（避免引用污染）
// 6️⃣ CopyOption（行为控制）
// 7️⃣ 高性能设计（sync.Map缓存）
// 8️⃣ 高灵活性设计（支持仅拷贝或排除某些字段）
func CopyAttribute(dst any, src any, opts ...CopyOption) error {
	var options CopyOptions
	for _, opt := range opts {
		opt(&options)
	}

	dstVal := reflect.ValueOf(dst)
	if dstVal.Kind() != reflect.Pointer || dstVal.Elem().Kind() != reflect.Struct {
		return ErrInvalidDst
	}
	dstVal = dstVal.Elem()

	srcVal := reflect.ValueOf(src)
	if srcVal.Kind() == reflect.Pointer {
		if srcVal.IsNil() {
			return nil
		}
		srcVal = srcVal.Elem()
	}
	if srcVal.Kind() != reflect.Struct {
		return ErrInvalidSrc
	}

	dstType := dstVal.Type()
	srcType := srcVal.Type()

	dstFields := cachedFields(dstType, options.Tag)
	srcFields := cachedFields(srcType, options.Tag)

	for name, dstInfo := range dstFields {
		if options.Fields != nil {
			if _, ok := options.Fields[name]; !ok {
				continue
			}
		}
		if options.Exclude != nil {
			if _, ok := options.Exclude[name]; ok {
				continue
			}
		}

		srcInfo, ok := srcFields[name]
		if !ok {
			continue
		}

		dstf := dstVal.FieldByIndex(dstInfo.index)
		srcf := srcVal.FieldByIndex(srcInfo.index)

		if !dstf.CanSet() {
			continue
		}

		if options.IgnoreZero && isZeroValue(srcf) {
			continue
		}

		assignDeepValue(dstf, srcf)
	}

	return nil
}

// assignDeepValue 深拷贝复制 指针、map、slice
func assignDeepValue(dst, src reflect.Value) {
	if src.Kind() == reflect.Pointer {
		if src.IsNil() {
			return
		}
		src = src.Elem()
	}

	if dst.Kind() == reflect.Pointer {
		if dst.IsNil() {
			dst.Set(reflect.New(dst.Type().Elem()))
		}
		dst = dst.Elem()
	}

	if src.Type().AssignableTo(dst.Type()) {
		dst.Set(src)
		return
	}

	if src.Type().ConvertibleTo(dst.Type()) {
		dst.Set(src.Convert(dst.Type()))
		return
	}

	switch src.Kind() {
	case reflect.Slice:
		dst.Set(deepCopySlice(src, dst.Type()))
	case reflect.Map:
		dst.Set(deepCopyMap(src, dst.Type()))
	default:
		assignValue(dst, src)
	}
}

// deepCopySlice 深拷贝复制 Slice
func deepCopySlice(src reflect.Value, dstType reflect.Type) reflect.Value {
	n := src.Len()
	dst := reflect.MakeSlice(dstType, n, n)
	for i := 0; i < n; i++ {
		assignDeepValue(dst.Index(i), src.Index(i))
	}
	return dst
}

// deepCopyMap 深拷贝复制 Map
func deepCopyMap(src reflect.Value, dstType reflect.Type) reflect.Value {
	dst := reflect.MakeMapWithSize(dstType, src.Len())
	iter := src.MapRange()
	for iter.Next() {
		k := reflect.New(dstType.Key()).Elem()
		v := reflect.New(dstType.Elem()).Elem()
		assignDeepValue(k, iter.Key())
		assignDeepValue(v, iter.Value())
		dst.SetMapIndex(k, v)
	}
	return dst
}

// isZeroValue 判断是否零值
func isZeroValue(v reflect.Value) bool {
	return reflect.DeepEqual(v.Interface(), reflect.Zero(v.Type()).Interface())
}

// 缓存出现的结构体字段
func cachedFields(t reflect.Type, tag string) map[string]fieldInfo {
	cacheKey := t.String() + "|" + tag
	if v, ok := fieldCache.Load(cacheKey); ok {
		return v.(map[string]fieldInfo)
	}

	fields := make(map[string]fieldInfo)
	collectFields(t, tag, nil, fields)

	fieldCache.Store(cacheKey, fields)
	return fields
}

// 收集所有字段
func collectFields(t reflect.Type, tag string, parent []int, out map[string]fieldInfo) {
	for i := 0; i < t.NumField(); i++ {
		sf := t.Field(i)

		if !sf.IsExported() {
			continue
		}

		index := append(parent, i)

		// 匿名嵌入 struct → 递归展开
		if sf.Anonymous && sf.Type.Kind() == reflect.Struct {
			collectFields(sf.Type, tag, index, out)
			continue
		}

		name := sf.Name

		// tag 映射
		if tag != "" {
			if tv := sf.Tag.Get(tag); tv != "" && tv != "-" {
				name = strings.Split(tv, ",")[0]
			}
		}

		out[name] = fieldInfo{index: index}
	}
}

// assignValue 赋值基本数据类型
func assignValue(dst, src reflect.Value) {
	if !src.IsValid() {
		return
	}

	// 处理指针
	if src.Kind() == reflect.Pointer {
		if src.IsNil() {
			return
		}
		src = src.Elem()
	}

	if dst.Kind() == reflect.Pointer {
		if dst.IsNil() {
			dst.Set(reflect.New(dst.Type().Elem()))
		}
		dst = dst.Elem()
	}

	srcType := src.Type()
	dstType := dst.Type()

	// 1️⃣ 直接赋值
	if srcType.AssignableTo(dstType) {
		dst.Set(src)
		return
	}

	// 2️⃣ 可转换类型（核心修复）
	if srcType.ConvertibleTo(dstType) {
		dst.Set(src.Convert(dstType))
		return
	}

	// 3️⃣ 数值类型互转（int / uint / float / bool）
	if isNumber(src.Kind()) && isNumber(dst.Kind()) {
		dst.Set(convertNumber(src, dstType))
		return
	}

	// 4️⃣ string <-> []byte
	if src.Kind() == reflect.String && dst.Kind() == reflect.Slice && dstType.Elem().Kind() == reflect.Uint8 {
		dst.SetBytes([]byte(src.String()))
		return
	}
	if src.Kind() == reflect.Slice && srcType.Elem().Kind() == reflect.Uint8 && dst.Kind() == reflect.String {
		dst.SetString(string(src.Bytes()))
		return
	}
}

// isNumber 判断某个字段是否为数字类型
func isNumber(k reflect.Kind) bool {
	switch k {
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64,
		reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64,
		reflect.Float32, reflect.Float64, reflect.Bool:
		return true
	default:
		return false
	}
}

// convertNumber 数值类型的数据之间互转
func convertNumber(src reflect.Value, dstType reflect.Type) reflect.Value {
	dst := reflect.New(dstType).Elem()

	switch dst.Kind() {
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		dst.SetInt(src.Convert(reflect.TypeOf(int64(0))).Int())
	case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
		dst.SetUint(src.Convert(reflect.TypeOf(uint64(0))).Uint())
	case reflect.Float32, reflect.Float64:
		dst.SetFloat(src.Convert(reflect.TypeOf(float64(0))).Float())
	case reflect.Bool:
		dst.SetBool(src.Convert(reflect.TypeOf(bool(false))).Bool())
	default:
		panic("unhandled default case")
	}

	return dst
}
