package main

import (
	"fmt"
	"generator-go/router"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"net/http"
)

func main() {
	r := gin.Default()

	// 启用跨域支持
	r.Use(cors.Default())

	// 路由分组，所有需要鉴权的接口用 AuthMiddleware 包裹
	genGroup := r.Group("/gen", AuthMiddleware())
	{
		genGroup.GET("/connect", router.Connect)
		genGroup.GET("/closeConnect", router.CloseConnect)
		genGroup.GET("/parse", router.Parse)
		genGroup.GET("/genCode", router.GenCode)
		genGroup.GET("/preview", router.Preview)
	}

	r.Run(":8080")
}

// @Tags 登录登出模块
// @Summary 登录-Summary
// @Description 登录-Description
// @Accept  json
// @Produce json
// @Param   username   path    string     true        "登录用户名"
// @Param   password   path    string     true        "登录密码"
// @Success 200 {string} string    "ok"
// @Router /login [post]
func login(c *gin.Context) {
	username := c.PostForm("username")
	password := c.PostForm("password")
	c.String(http.StatusOK, "Hello world "+username+"_"+password)
}

// @Tags 登录登出模块
// @Summary 退出-Summary
// @Description 退出-Description
// @Security BearerAuth
// @Accept  json
// @Produce json
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse  "退出登录响应数据"
// @Router /logout [post]
func logout(c *gin.Context) {

	authorization := c.GetHeader("Authorization")
	username := c.PostForm("username")

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

func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		token := c.GetHeader("Authorization")
		if token == "" || !validateToken(token) {
			c.JSON(401, gin.H{"error": "Unauthorized"})
			c.Abort()
			return
		}
		c.Next()
	}
}

func validateToken(token string) bool {
	fmt.Printf("AuthMiddleware.validateToken = %s \n", token)
	return true
}

/*
	下面将介绍如何给接口加上请求投认证信息
    @Security BearerAuth：表示这个接口要加Bearer类型的认证信息
	@Failure 401：表示接口返回401时，是因为未授权的原因

✅ 小贴士
@securityDefinitions.apikey 必须放在 main.go 里，或者是生成文档的入口文件中；
@Security BearerAuth 每个需要认证的接口都要单独加；
可以封装一个统一的响应结构体返回。

*/

// @Tags 用户模块
// @Summary 获取用户详细-Summary
// @Description 获取用户详细-Description
// @Security BearerAuth
// @Param username query string true "登录用户名"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /getUser [get]
func getUser(c *gin.Context) {
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
// @Summary 创建用户-Summary
// @Description 提交用户信息创建用户-Description
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param user body UserDto true "用户信息"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "退出登录响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /postUser [post]
func postUser(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	var dto UserDto
	c.ShouldBindJSON(&dto)

	fmt.Printf("postUser，authorization = %s, dto = %v \n", authorization, dto)

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
// @Summary 更新用户信息-Summary
// @Description 更新用户信息-Description
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path int true "用户ID"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "更新用户信息-响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /putUser/{userId} [put]
func putUser(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	var dto UserDto
	c.ShouldBindJSON(&dto)

	fmt.Printf("putUser，authorization = %s, dto = %v \n", authorization, dto)

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
// @Summary 删除用户-Summary
// @Description 删除用户-Description
// @Security BearerAuth
// @Param id path int true "用户ID"
// @Success 200 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string}  "更新用户信息-响应数据"
// @Failure 401 {object} JsonResponse{data=LogoutVo,msg=string,code=int,error=string} "未授权"
// @Router /deleteUser/{userId} [delete]
func deleteUser(c *gin.Context) {
	authorization := c.GetHeader("Authorization")
	userId := c.Param("userId") // 取出字符串形式

	fmt.Printf("deleteUser，authorization = %s, userId = %s \n", authorization, userId)

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

// 接口统一响应结构
type JsonResponse struct {
	Code    int    `json:"code"`    // 响应码
	Msg     string `json:"msg"`     // 失败消息
	Success bool   `json:"success"` // 是否操作成功，操作成功返回true，否则返回false
	Data    any    `json:"data"`    // 响应数据
}

type LogoutVo struct {
	Foo string `json:"foo"` // 测试的字段
}

type UserDto struct {
	Username string `json:"username"` // 用户名
	Password string `json:"password"` // 密码
	Foo      string `json:"foo"`      // 测试的字段
}
