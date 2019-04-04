#test_setup
class eidas_sidp_proxy::test_setup inherits eidas_sidp_proxy{

  include platform

  if ($platform::test_setup) {
    file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/${eidas_sidp_proxy::key_store['name']}":
      ensure => 'file',
      source => "puppet:///modules/${module_name}/${eidas_sidp_proxy::key_store['name']}",
      group  => $eidas_sidp_proxy::service_name,
      owner  => $eidas_sidp_proxy::service_name,
      mode   => '0644',
    }
    file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/${eidas_sidp_proxy::trust_store['name']}":
      ensure => 'file',
      source => "puppet:///modules/${module_name}/${eidas_sidp_proxy::trust_store['name']}",
      group  => $eidas_sidp_proxy::service_name,
      owner  => $eidas_sidp_proxy::service_name,
      mode   => '0644',
    }
    file { "${eidas_sidp_proxy::config_dir}${eidas_sidp_proxy::application}/idPortenKeystore.jks":
      ensure => 'file',
      source => "puppet:///modules/${module_name}/idPortenKeystore.jks",
      group  => $eidas_sidp_proxy::service_name,
      owner  => $eidas_sidp_proxy::service_name,
      mode   => '0644',
    }
  }
}