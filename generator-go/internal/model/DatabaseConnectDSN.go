package model

// DatabaseConnectDSN 数据库链接信息
type DatabaseConnectDSN struct {
	Host     string `json:"host"`                      // 服务器地址
	Port     string `json:"port"`                      // 数据库连接端口
	UserName string `json:"userName"`                  // 数据库用户名
	Password string `json:"password"`                  // 数据库密码
	DBName   string `json:"dbName" binding:"required"` // 数据库名
}

// 拼接数据库链接配置
func (dc *DatabaseConnectDSN) Dsn() string {
	// dsn := "用户名:密码@tcp(IP:Port)/数据库名?charset=utf8mb4&parseTime=True&loc=Local"
	//dsn := "root:root123@tcp(127.0.0.1:3306)/test?charset=utf8mb4&parseTime=True&loc=Local"

	// "root:root123@tcp(127.0.0.1:3306)/test?charset=utf8&parseTime=True"
	return dc.UserName + ":" + dc.Password + "@tcp(" + dc.Host + ":" + dc.Port + ")/" + dc.DBName + "?charset=utf8mb4&parseTime=True&loc=Local"
}
