package middleware

import (
	"generator-go/internal/model"
	"github.com/gin-gonic/gin"
	"net/http"
)

func AuthorizationMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")

		// 没有加密头或为 false，跳过加解密
		if authHeader == "" {
			// 如果加密失败，返回错误
			c.AbortWithStatusJSON(http.StatusInternalServerError, model.Error("获取会话数据失败!!!"))
			return
		}

		c.Next()
	}
}
