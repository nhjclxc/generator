package excelutils

import (
	"errors"
	"fmt"
	"github.com/xuri/excelize/v2"
	"math/big"
	"mime/multipart"
	"reflect"
	"strconv"
	"strings"
	"time"
)

// go get -u github.com/xuri/excelize/v2

type ExcelOptions struct {
	ColWidth     float64
	RowHeight    float64
	FirstColAxis int32
	FirstRowAxis int
}

// 设置单元格内容
func writeCell(file *excelize.File, sheetName string, collAxis int32, rowAxis int, value interface{}) {
	// SetCellValue：填写的单元格下标，先列下标，再行下标。例如，第一行第一列：A1，第二行第三列：C2。
	err := file.SetCellValue(sheetName, getCellIndex(collAxis, rowAxis), value)
	if err != nil {
		fmt.Printf("excel set cell value error:%v \n", err)
	}
}

// 获取单元格下标
func getCellIndex(colAxis int32, rowAxis int) string {
	return fmt.Sprintf("%c%d", colAxis+97, rowAxis)
}

// 设置样式
func setStyle(file *excelize.File, sheetName string, maxCol int32, maxRow int, options ExcelOptions) {
	err := file.SetColWidth(sheetName, string(options.FirstColAxis), string(maxCol), options.ColWidth) // 设置列宽
	if err != nil {
		fmt.Printf("excel SetColWidth error:%v \n", err)
	}
	err = file.SetRowHeight(sheetName, options.FirstRowAxis, options.RowHeight) // 设置行高
	if err != nil {
		fmt.Printf("excel SetRowHeight error:%v \n", err)
	}
	style := &excelize.Style{
		Font: &excelize.Font{
			Bold: true,
		},
	}
	styleId, err := file.NewStyle(style)
	if err != nil {
		fmt.Printf("excel NewStyle error:%v \n", err)
	}
	err = file.SetCellStyle(sheetName, getCellIndex(options.FirstColAxis, options.FirstRowAxis), getCellIndex(maxCol, maxRow),
		styleId)
	if err != nil {
		fmt.Printf("excel SetCellStyle error:%v \n", err)
	}
}

// ImportExcelFile 导入文件里面的数据
func ImportExcelFile[T any](filename string, firstColAxis int32, firstRowAxis int, headerKeys []string) ([]*T, error) {
	// 1. 打开 Excel 文件
	f, err := excelize.OpenFile(filename)
	if err != nil {
		return nil, fmt.Errorf("读取Excel失败: %w", err)
	}
	defer f.Close()

	// 表面看是类型一样，但实际上 Go 在泛型设计中，每次使用 T 都是独立实例化的类型参数作用域。
	// 类型参数 T 是局部的，只在当前函数内有效
	//Go 中，每个函数的 [T any] 都是自己独立定义的类型变量，即使名字相同，编译器也认为它们是不同的类型：

	// 显式传递泛型参数
	return ImportExcel[T](f, firstColAxis, firstRowAxis, headerKeys)
}

// ImportExcelByte 导入接口里面的数据 multipart.File
func ImportExcelByMultiFile[T any](file multipart.File, firstColAxis int32, firstRowAxis int, headerKeys []string) ([]*T, error) {
	f, err := excelize.OpenReader(file)
	if err != nil {
		return nil, fmt.Errorf("解析Excel失败: %w", err)
	}
	defer f.Close()

	// 显式传递泛型参数
	return ImportExcel[T](f, firstColAxis, firstRowAxis, headerKeys)
}

// 执行导入
func ImportExcel[T any](f *excelize.File, firstColAxis int32, firstRowAxis int, headerKeys []string) ([]*T, error) {

	// 2. 获取默认工作表名称（如 Sheet1）
	sheetName := f.GetSheetName(f.GetActiveSheetIndex())

	// 3. 获取所有行的数据（按行读取）
	rows, err := f.GetRows(sheetName)
	if err != nil {
		return nil, err
	}

	testObjectList := newStructSlice[T](len(rows)-firstRowAxis, len(rows)-firstRowAxis)

	// headerKeys 和 headerValues必须一一对应

	// 4. 遍历行与列
	for rowIndex, row := range rows {
		if rowIndex < firstRowAxis {
			continue
		}

		testObject := newStructInstance[T]()
		for colIndex, cell := range row {
			if int32(colIndex) < firstColAxis {
				break
			}
			err := setFieldByName(testObject, headerKeys[colIndex], cell)
			if err != nil {
				fmt.Println("设置错误", err)
				break
			}
		}
		testObjectList[rowIndex-firstRowAxis] = testObject
	}

	return testObjectList, nil
}

// ExportExcel 执行导出
func ExportExcel[T any](data []T, firstColAxis int32, firstRowAxis int, headerKeys []string, headerValues []string) *excelize.File {
	// 1. 导入依赖包  "github.com/xuri/excelize/v2"

	//2. 创建实例
	sheetName := "Sheet1"

	file := excelize.NewFile()
	sheetIndex, _ := file.NewSheet(sheetName)
	file.SetActiveSheet(sheetIndex) // 默认sheet

	// headerKeys 和 headerValues必须一一对应

	// 3. 设置表头
	for i, headerName := range headerValues {
		tempColAxis := firstColAxis + int32(i)
		writeCell(file, sheetName, tempColAxis, firstRowAxis, headerName)
	}

	// 4. 填充数据
	//data := getTestObject()
	for i, item := range data {
		tempRowAxis := firstRowAxis + i + 1
		for j, header := range headerKeys {
			value := getFieldByName(&item, header)
			tempColAxis := firstColAxis + int32(j)
			writeCell(file, sheetName, tempColAxis, tempRowAxis, value)
		}
	}

	////6. 写到输出流
	//// 保存为文件
	//err := file.SaveAs("output.xlsx")
	//if err != nil {
	//	fmt.Println("保存失败：", err)
	//}

	return file
}

// 泛型通用方法：通过字段名获取任意结构体的字段值
func getFieldByName[T any](obj *T, fieldName string) interface{} {
	v := reflect.ValueOf(obj).Elem()
	if v.Kind() == reflect.Ptr {
		v = v.Elem() // 解引用指针
	}
	f := v.FieldByName(fieldName)
	if !f.IsValid() {
		return nil
	}
	if f.Type() == reflect.TypeOf(time.Time{}) {
		//fmt.Println("字段是 time.Time 类型")
		t, ok := f.Interface().(time.Time)
		if ok {
			return formatTime(t)
		}
	}
	return f.Interface()
}

func formatTime(t time.Time) string {
	return t.Format("2006-01-02 15:04:05")
}

func parseTime(s string) (time.Time, error) {
	return time.Parse("2006-01-02 15:04:05", s)
}

// 泛型方法：通过字段名设置任意结构体字段的值
func setFieldByName[T any](obj *T, fieldName string, value interface{}) error {
	v := reflect.ValueOf(obj)
	if v.Kind() != reflect.Ptr || v.IsNil() {
		return errors.New("obj must be a non-nil pointer to struct")
	}

	elem := v.Elem()
	if elem.Kind() != reflect.Struct {
		return errors.New("obj must be a pointer to struct")
	}

	field := elem.FieldByName(fieldName)
	if !field.IsValid() {
		return fmt.Errorf("field '%s' does not exist", fieldName)
	}
	if !field.CanSet() {
		return fmt.Errorf("field '%s' cannot be set", fieldName)
	}

	// 自动转换 value 到目标字段类型
	targetType := field.Type()
	converted, err := convertValue(value, targetType)
	if err != nil {
		return fmt.Errorf("failed to convert value: %v", err)
	}

	field.Set(converted)
	return nil
}

// 类型转换逻辑
func convertValue(value interface{}, targetType reflect.Type) (reflect.Value, error) {
	val := reflect.ValueOf(value)

	// 如果类型可直接转换
	if val.Type().ConvertibleTo(targetType) {
		return val.Convert(targetType), nil
	}

	// 字符串类型处理
	if str, ok := value.(string); ok {
		switch targetType.Kind() {
		case reflect.String:
			return reflect.ValueOf(str), nil
		case reflect.Int, reflect.Int32, reflect.Int64:
			i, err := strconv.ParseInt(str, 10, 64)
			if err != nil {
				return reflect.Value{}, err
			}
			return reflect.ValueOf(i).Convert(targetType), nil
		case reflect.Float32, reflect.Float64:
			f, err := strconv.ParseFloat(str, 64)
			if err != nil {
				return reflect.Value{}, err
			}
			return reflect.ValueOf(f).Convert(targetType), nil
		case reflect.Bool:
			b, err := strconv.ParseBool(str)
			if err != nil {
				if str == "1" || strings.ToUpper(str) == "TRUE" {
					b = true
				} else if str == "0" || strings.ToUpper(str) == "FALSE" {
					b = false
				} else {
					return reflect.Value{}, err
				}
			}
			return reflect.ValueOf(b), nil

		default:
			// time.Time 特殊判断
			if targetType == reflect.TypeOf(time.Time{}) {
				t, err := parseTime(str)
				if err != nil {
					return reflect.Value{}, err
				}
				return reflect.ValueOf(t), nil
			}
			// big.Float{} 特殊判断
			if targetType == reflect.TypeOf(&big.Float{}) {
				bf := new(big.Float)
				bf, ok := bf.SetString(str)
				if !ok {
					return reflect.Value{}, fmt.Errorf("cannot parse '%s' to *big.Float", str)
				}
				return reflect.ValueOf(bf), nil
			}
			fmt.Printf("default: %v \n", value)
		}
	}

	// 其他 fallback 情况
	return reflect.Value{}, fmt.Errorf("unsupported conversion from %T to %s", value, targetType.String())
}

// 创建泛型对象
func newStructInstance[T any]() *T {
	return new(T)
}

// 创建泛型切片
func newStructSlice[T any](length, capacity int) []*T {
	return make([]*T, length, capacity)
}
