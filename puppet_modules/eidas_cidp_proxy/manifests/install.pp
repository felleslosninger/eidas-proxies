#install.pp
class eidas_cidp_proxy::install inherits eidas_cidp_proxy {

  user { $eidas_cidp_proxy::service_name:
    ensure => present,
    shell  => '/sbin/nologin',
    home   => '/',
  } ->
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}":
    ensure => 'directory',
    owner  => $eidas_cidp_proxy::service_name,
    group  => $eidas_cidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/messages":
    ensure => 'directory',
    owner  => $eidas_cidp_proxy::service_name,
    group  => $eidas_cidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/config":
    ensure => 'directory',
    owner  => $eidas_cidp_proxy::service_name,
    group  => $eidas_cidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_cidp_proxy::log_root}${eidas_cidp_proxy::application}":
    ensure => 'directory',
    owner  => $eidas_cidp_proxy::service_name,
    group  => $eidas_cidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_cidp_proxy::install_dir}${eidas_cidp_proxy::application}":
    ensure => 'directory',
    mode   => '0644',
    owner  => $eidas_cidp_proxy::service_name,
    group  => $eidas_cidp_proxy::service_name,
  } ->
  file { $eidas_cidp_proxy::tomcat_tmp_dir:
    ensure => 'directory',
    mode   => '0755',
    owner  => $eidas_cidp_proxy::service_name,
    group  => $eidas_cidp_proxy::service_name,
  }
  difilib::spring_boot_logrotate { $eidas_cidp_proxy::application:
    application => $eidas_cidp_proxy::application,
  }

  if ($platform::install_cron_jobs) {
    $log_cleanup_command = "find ${eidas_cidp_proxy::log_root}${eidas_cidp_proxy::application}/ -type f -name \"*.gz\" -mtime +7 -exec rm -f {} \\;"
    $auditlog_cleanup_command = "find ${eidas_cidp_proxy::log_root}${eidas_cidp_proxy::application}/audit/ -type f -name \"*audit.log\" -mtime +7 -exec rm -f {} \\;"

    cron { "${eidas_cidp_proxy::application}_log_cleanup":
      command => $log_cleanup_command,
      user    => 'root',
      hour    => '03',
      minute  => '00',
    } ->
    cron { "${eidas_cidp_proxy::application}_log_cleanup_audit":
      command => $auditlog_cleanup_command,
      user    => 'root',
      hour    => '03',
      minute  => '05',
    }
  }
}
