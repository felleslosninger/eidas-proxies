#install.pp
class eidas_sidp_proxy::install inherits eidas_sidp_proxy {

  user { $eidas_sidp_proxy::service_name:
    ensure => present,
    shell  => '/sbin/nologin',
    home   => '/',
  } ->
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}":
    ensure => 'directory',
    owner  => $eidas_sidp_proxy::service_name,
    group  => $eidas_sidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/messages":
    ensure => 'directory',
    owner  => $eidas_sidp_proxy::service_name,
    group  => $eidas_sidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_sidp_proxy::log_root}${eidas_sidp_proxy::application}":
    ensure => 'directory',
    owner  => $eidas_sidp_proxy::service_name,
    group  => $eidas_sidp_proxy::service_name,
    mode   => '0755',
  } ->
  file { "${eidas_sidp_proxy::log_root}${eidas_sidp_proxy::application}/audit":
    ensure => 'directory',
    mode   => '0755',
    owner  => $eidas_sidp_proxy::service_name,
    group  => $eidas_sidp_proxy::service_name,
  } ->
  file { "${eidas_sidp_proxy::install_dir}${eidas_sidp_proxy::application}":
    ensure => 'directory',
    mode   => '0644',
    owner  => $eidas_sidp_proxy::service_name,
    group  => $eidas_sidp_proxy::service_name,
  } ->
  file { $eidas_sidp_proxy::tomcat_tmp_dir:
    ensure => 'directory',
    mode   => '0755',
    owner  => $eidas_sidp_proxy::service_name,
    group  => $eidas_sidp_proxy::service_name,
  }
  difilib::spring_boot_logrotate { $eidas_sidp_proxy::application:
    application => $eidas_sidp_proxy::application,
  }

  if ($platform::install_cron_jobs) {
    $log_cleanup_command = "find ${eidas_sidp_proxy::log_root}${eidas_sidp_proxy::application}/ -type f -name \"*.gz\" -mtime +7 -exec rm -f {} \\;"
    $auditlog_cleanup_command = "find ${eidas_sidp_proxy::log_root}${eidas_sidp_proxy::application}/audit/ -type f -name \"*audit.log\" -mtime +7 -exec rm -f {} \\;"

    cron { "${eidas_sidp_proxy::application}_log_cleanup":
      command => $log_cleanup_command,
      user    => 'root',
      hour    => '03',
      minute  => '00',
    } ->
    cron { "${eidas_sidp_proxy::application}_log_cleanup_audit":
      command => $auditlog_cleanup_command,
      user    => 'root',
      hour    => '03',
      minute  => '05',
    }
  }
}
