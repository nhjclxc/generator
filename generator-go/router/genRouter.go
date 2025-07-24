package router

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"net/http"
)

// @Tags 代码生成模块
// @Summary 连接到数据库-Summary
// @Description 连接到数据库-Description
// @Security BearerAuth
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /connect [get]
func Connect(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	username := c.Query("username")

	fmt.Printf("getUser，authorization = %s, username = %s \n", authorization, username)

	c.JSON(
		http.StatusOK,
		gin.H{
			"code":  http.StatusOK,
			"error": nil,
			"msg":   "操作成功",
			"data":  LogoutVo{Foo: "Foo"},
		},
	)
}

// @Tags 用户模块
// @Summary 获取用户详细-Summary
// @Description 获取用户详细-Description
// @Security BearerAuth
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /connect [get]
func CloseConnect(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	username := c.Query("username")

	fmt.Printf("getUser，authorization = %s, username = %s \n", authorization, username)

	c.JSON(
		http.StatusOK,
		gin.H{
			"code":  http.StatusOK,
			"error": nil,
			"msg":   "操作成功",
			"data":  LogoutVo{Foo: "Foo"},
		},
	)
}

// @Tags 用户模块
// @Summary 获取用户详细-Summary
// @Description 获取用户详细-Description
// @Security BearerAuth
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /connect [get]
func Parse(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	username := c.Query("username")

	fmt.Printf("getUser，authorization = %s, username = %s \n", authorization, username)

	c.JSON(
		http.StatusOK,
		gin.H{
			"code":  http.StatusOK,
			"error": nil,
			"msg":   "操作成功",
			"data":  LogoutVo{Foo: "Foo"},
		},
	)
}

// @Tags 用户模块
// @Summary 获取用户详细-Summary
// @Description 获取用户详细-Description
// @Security BearerAuth
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /connect [get]
func GenCode(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	username := c.Query("username")

	fmt.Printf("getUser，authorization = %s, username = %s \n", authorization, username)

	c.JSON(
		http.StatusOK,
		gin.H{
			"code":  http.StatusOK,
			"error": nil,
			"msg":   "操作成功",
			"data":  LogoutVo{Foo: "Foo"},
		},
	)
}

// @Tags 用户模块
// @Summary 获取用户详细-Summary
// @Description 获取用户详细-Description
// @Security BearerAuth
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /connect [get]
func Preview(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	username := c.Query("username")

	fmt.Printf("getUser，authorization = %s, username = %s \n", authorization, username)

	c.JSON(
		http.StatusOK,
		gin.H{
			"code":  http.StatusOK,
			"error": nil,
			"msg":   "操作成功",
			"data":  LogoutVo{Foo: "Foo"},
		},
	)
}
