module.exports = {
  apps: [
    {
      name: 'shopping-backend',
      script: 'java',
      args: '-jar -Xms512m -Xmx1024m ./backend/target/shopping-1.0.0.jar',
      cwd: './',
      instances: 1,
      autorestart: true,
      watch: false,
      max_memory_restart: '1200M',
      env: {
        SPRING_PROFILES_ACTIVE: 'default',
        TZ: 'Asia/Shanghai'
      },
      log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
      combine_logs: true,
      error_file: './logs/backend-error.log',
      out_file: './logs/backend-out.log'
    }
  ]
};
