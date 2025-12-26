package copier

import (
	"fmt"
	"testing"
)

type Base struct {
	CreateAt int64 `json:"create_at"`
	Remark   string
}
type Src struct {
	ID        int64            `json:"id"`
	Name      string           `json:"name"`
	Age       int8             `json:"age"`   // int8 -> int
	Score     float32          `json:"score"` // float32 -> float64
	Enable    bool             `json:"enable"`
	Note      *string          `json:"note"` // *string -> string
	Data      []byte           `json:"data"` // []byte -> string
	Tags      []int32          `json:"tags"` // []int32 -> []int
	Meta      map[string]int64 `json:"meta"` // map 深拷贝
	Extra     map[string]*int  `json:"extra"`
	CreatedAt int64            `json:"created_at"` // int64 -> int
	Base      `json:",inline"` // 匿名嵌入
}
type Dst struct {
	ID        int     `json:"id"` // int64 -> int
	Name      string  `json:"name"`
	Age       int     `json:"age"`
	Score     float64 `json:"score"`
	Enable    bool
	Note      string         `json:"note"`  // *string -> string
	Data      []byte         `json:"data"`  // []byte -> string
	Tags      []int          `json:"tags"`  // slice 深拷贝
	Meta      map[string]int `json:"meta"`  // map 深拷贝
	Extra     map[string]int `json:"extra"` // *int -> int
	CreatedAt int            `json:"created_at"`
	Base                     // 匿名嵌入
}

func mockSrc() Src {
	note := "hello"
	extra := 99

	return Src{
		ID:        10001,
		Name:      "Alice",
		Age:       18,
		Score:     95.5,
		Enable:    true,
		Note:      &note,
		Data:      []byte("binary-data"),
		Tags:      []int32{1, 2, 3},
		Meta:      map[string]int64{"a": 1, "b": 2},
		Extra:     map[string]*int{"x": &extra},
		CreatedAt: 1700000000,
		Base: Base{
			CreateAt: 10123456000,
			Remark:   "from base",
		},
	}
}

func TestName(t *testing.T) {

	src := mockSrc()
	var dst Dst

	err := CopyAttribute(
		&dst,
		src,
		WithTag("json"),
		WithIgnoreZero(),
	)
	if err != nil {
		panic(err)
	}

	fmt.Printf("%+v\n", dst)
}

func BenchmarkCopyAttribute(b *testing.B) {
	var dst Dst
	var src Src

	for i := 0; i < b.N; i++ {
		CopyAttribute(&dst, src)
	}
}

func BenchmarkCopier(b *testing.B) {
	var dst Dst
	var src Src

	for i := 0; i < b.N; i++ {
		CopyAttribute(&dst, &src)
	}
}

//
//func BenchmarkMapstructure(b *testing.B) {
//	var dst Dst
//	var src Src
//
//	for i := 0; i < b.N; i++ {
//		mapstructure.Decode(src, &dst)
//	}
//}
