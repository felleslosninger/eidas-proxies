#test_setup
class eidas_cidp_proxy::test_setup inherits eidas_cidp_proxy{

  include platform

  if ($platform::test_setup) {
    file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/${eidas_cidp_proxy::key_store['name']}":
      ensure => 'file',
      source => "puppet:///modules/${module_name}/${eidas_cidp_proxy::key_store['name']}",
      group  => $eidas_cidp_proxy::service_name,
      owner  => $eidas_cidp_proxy::service_name,
      mode   => '0644',
    }
    file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/${eidas_cidp_proxy::trust_store['name']}":
      ensure => 'file',
      source => "puppet:///modules/${module_name}/${eidas_cidp_proxy::trust_store['name']}",
      group  => $eidas_cidp_proxy::service_name,
      owner  => $eidas_cidp_proxy::service_name,
      mode   => '0644',
    }
    file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/cidpproxykeystore.jks":
      ensure => 'file',
      source => "puppet:///modules/${module_name}/cidpproxykeystore.jks",
      group  => $eidas_cidp_proxy::service_name,
      owner  => $eidas_cidp_proxy::service_name,
      mode   => '0644',
    }
    file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/idp_metadata.xml":
      ensure  => 'file',
      content => template("${module_name}/idp_metadata.xml.erb"),
      group   => $eidas_cidp_proxy::service_name,
      owner   => $eidas_cidp_proxy::service_name,
      mode    => '0644',
    }
    file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/sp_metadata.xml":
      content => template("${module_name}/sp_metadata.xml.erb"),
      group   => $eidas_cidp_proxy::service_name,
      owner   => $eidas_cidp_proxy::service_name,
      mode    => '0644',
    }
  }

}