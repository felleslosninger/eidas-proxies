class eidas_sidp_proxy::config inherits eidas_sidp_proxy{

  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/application.yaml":
    ensure  => 'file',
    content => template("${module_name}/application.yaml.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }->
  file { "/etc/rc.d/init.d/${eidas_sidp_proxy::service_name}":
    ensure => 'link',
    target => "${eidas_sidp_proxy::install_dir}${eidas_sidp_proxy::application}/${eidas_sidp_proxy::artifact_id}.war",
  }
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/SamlEngine.xml":
    ensure  => 'file',
    content => template("${module_name}/SamlEngine.xml.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/SignModule.properties":
    ensure  => 'file',
    content => template("${module_name}/SignModule.properties.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_sidp_proxy::install_dir}${eidas_sidp_proxy::application}/${eidas_sidp_proxy::artifact_id}.conf":
    ensure  => 'file',
    content => template("${module_name}/${eidas_sidp_proxy::artifact_id}.conf.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/EncryptModule_Conf.xml":
    ensure  => 'file',
    content => template("${module_name}/EncryptModule_Conf.xml.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/encryptionConf.xml":
    ensure  => 'file',
    content => template("${module_name}/encryptionConf.xml.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/SamlEngine_eidas-sidp-proxy.xml":
    ensure  => 'file',
    content => template("${module_name}/SamlEngine_eidas-sidp-proxy.xml.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/countriesAttributes.json":
    ensure  => 'file',
    content => template("${module_name}/countriesAttributes.json.erb"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  } ->
  difilib::logback_config { $eidas_sidp_proxy::application:
    application       => $eidas_sidp_proxy::application,
    owner             => $eidas_sidp_proxy::service_name,
    group             => $eidas_sidp_proxy::service_name,
    resilience        => false,
    performance_class => '',
    loglevel_no       => $eidas_sidp_proxy::log_level,
    loglevel_nondifi  => $eidas_sidp_proxy::log_level,
  } ->
  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/messages/eidas-sidp-proxy-messages.properties":
    ensure  => 'file',
    content => template("${module_name}/messages/eidas-sidp-proxy-messages.properties"),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }

  file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/saml-engine-additional-attributes.xml":
    ensure  => 'file',
    content => template('eidas_additional_attribute/saml-engine-additional-attributes.xml.erb'),
    group   => $eidas_sidp_proxy::service_name,
    owner   => $eidas_sidp_proxy::service_name,
    mode    => '0644',
  }

}
